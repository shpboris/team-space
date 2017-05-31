#! /bin/bash
yum -y install wget
yum -y install zip unzip
yum -y install dos2unix

sudo su
mkdir /home/deployer
cd /home/deployer
useradd deployer
echo deployer:deployer123! | chpasswd
gpasswd -a deployer wheel

sed -ie 's/#PasswordAuthentication yes/PasswordAuthentication yes/g' /etc/ssh/sshd_config
sed -ie 's/#PasswordAuthentication no//g' /etc/ssh/sshd_config
service sshd reload

wget https://s3.amazonaws.com/aws-cli/awscli-bundle.zip
unzip awscli-bundle.zip
./awscli-bundle/install -i /usr/local/aws -b /usr/local/bin/aws
./awscli-bundle/install -b ~/bin/

aws s3 cp s3://$bucketName$/$tarFileName$.tar.gz $tarFileName$.tar.gz --region $regionName$
tar -zxvf $tarFileName$.tar.gz
cd $tarFileName$
chmod +x setup.sh
dos2unix setup.sh
./setup.sh