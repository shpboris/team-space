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

chown -R $user$:$user$ /home/$user$
sudo su - $user$
echo "Completed deploy at: "$(date +"%T") >> $log