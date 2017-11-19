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

echo "installing wget at: "$(date +"%T") >> $log
yum -y install wget
while ! [ -x /usr/bin/wget ]
do
    echo "wget is not installed yet at: "$(date +"%T") >> $log
    sleep 5
    yum -y install wget
done
echo "wget is finally installed: "$(date +"%T") >> $log

yum -y install zip unzip
echo "Completed zip/unzip install at: "$(date +"%T") >> $log
yum -y install dos2unix
echo "Completed dos2unix install at: "$(date +"%T") >> $log
yum -y install mysql
echo "Completed MySql client install at: "$(date +"%T") >> $log

sed -ie 's/#PasswordAuthentication yes/PasswordAuthentication yes/g' /etc/ssh/sshd_config
sed -ie 's/#PasswordAuthentication no//g' /etc/ssh/sshd_config
service sshd reload
echo "Completed sshd service reload at: "$(date +"%T") >> $log

rpm -Uvh https://s3.amazonaws.com/aaronsilber/public/authbind-2.1.1-0.1.x86_64.rpm
touch /etc/authbind/byport/443
chown $user$:$user$ /etc/authbind/byport/443
chmod 755 /etc/authbind/byport/443
touch /etc/authbind/byport/80
chown $user$:$user$ /etc/authbind/byport/80
chmod 755 /etc/authbind/byport/80
echo "Completed authbind config at: "$(date +"%T") >> $log

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

DB_MODE="$dbmode$"

if [ $DB_MODE == MYSQL ] || [ $DB_MODE == RDS ]
then
cat >dbdetails <<EOF

DB_URL="$dburl$"
DB_USER="$user$"
DB_PASSWORD="$pass$"
DB_DRIVER="com.mysql.jdbc.Driver"
EOF
fi



chmod +x setup.sh
dos2unix setup.sh

chown -R $user$:$user$ /home/$user$


if [ $DB_MODE == MYSQL ] || [ $DB_MODE == RDS ]
then
    echo "Testing DB connection at: "$(date +"%T") >> $log
    timeout 3 bash -c "</dev/tcp/$dbhost$/3306">/dev/null 2>&1
    while [ $? -ne 0 ]
    do
        echo "DB is not connected yet at: "$(date +"%T") >> $log
        sleep 5
        timeout 3 bash -c "</dev/tcp/$dbhost$/3306">/dev/null 2>&1
    done
    echo "DB is finally connected: "$(date +"%T") >> $log
fi


echo "Completed app extraction, ready to run a setup.sh at: "$(date +"%T") >> $log
./setup.sh

netstat -ln | grep ":80 " 2>&1 > /dev/null
while [ $? -ne 0 ]
do
    sleep 1
    netstat -ln | grep ":80 " 2>&1 > /dev/null
done
echo "Started the application at: "$(date +"%T") >> $log