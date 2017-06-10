#! /bin/bash

sudo su
mkdir /home/$user$
useradd $user$
echo $user$:$pass$ | chpasswd
gpasswd -a $user$ wheel
echo '$user$ ALL=(ALL:ALL) NOPASSWD:ALL' >> /etc/sudoers

log=/home/$user$/deployer.log
touch $log
echo "Started deploy at: "$(date) >> $log
yum -y install wget
echo "Completed wget install at: "$(date +"%T") >> $log
yum -y install zip unzip
echo "Completed zip/unzip install at: "$(date +"%T") >> $log
yum -y install dos2unix
echo "Completed dos2unix install at: "$(date +"%T") >> $log


sed -ie 's/#PasswordAuthentication yes/PasswordAuthentication yes/g' /etc/ssh/sshd_config
sed -ie 's/#PasswordAuthentication no//g' /etc/ssh/sshd_config
service sshd reload
echo "Completed sshd service reload at: "$(date +"%T") >> $log

cd /home/$user$
wget https://s3.amazonaws.com/aws-cli/awscli-bundle.zip
echo "Completed aws cli download at: "$(date +"%T") >> $log
unzip awscli-bundle.zip
./awscli-bundle/install -i /usr/local/aws -b /usr/local/bin/aws
./awscli-bundle/install -b ~/bin/
echo "Completed aws cli install at: "$(date +"%T") >> $log

echo "Started JRE download at: "$(date +"%T") >> $log
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jre-8u131-linux-x64.rpm"
echo "Completed JRE download at: "$(date +"%T") >> $log
yum -y localinstall jre-8u131-linux-x64.rpm
echo "Completed JRE install at: "$(date +"%T") >> $log

aws s3 cp s3://$bucketName$/$tarFileName$.tar.gz $tarFileName$.tar.gz --region $regionName$
echo "Completed app download from S3 at: "$(date +"%T") >> $log
tar -zxvf $tarFileName$.tar.gz
cd $tarFileName$
chmod +x setup.sh
dos2unix setup.sh
sudo su - $user$
echo "Completed app extraction, ready to run a setup.sh at: "$(date +"%T") >> $log
sudo ./setup.sh

netstat -ln | grep ":8888 " 2>&1 > /dev/null
while [ $? -ne 0 ]
do
    sleep 1
    netstat -ln | grep ":8888 " 2>&1 > /dev/null
done
echo "Started the application at: "$(date +"%T") >> $log