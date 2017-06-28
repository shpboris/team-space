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
sudo wget http://dev.mysql.com/get/mysql57-community-release-el7-7.noarch.rpm
echo "Completed MySQl rpm download at: "$(date +"%T") >> $log
sudo yum -y localinstall mysql57-community-release-el7-7.noarch.rpm
echo "Completed MySQl local install at: "$(date +"%T") >> $log
sudo yum -y install mysql-community-server
echo "Completed MySQl install at: "$(date +"%T") >> $log
sudo service mysqld start
echo "Started MySQl at: "$(date +"%T") >> $log


cat >dbsetup.sql <<EOF
uninstall plugin validate_password;
DELETE FROM mysql.user WHERE User='';
DROP DATABASE IF EXISTS test;
CREATE DATABASE teamspace;
CREATE USER '$user$'@'localhost' IDENTIFIED BY '$pass$';
GRANT ALL PRIVILEGES ON * . * TO '$user$'@'localhost';
FLUSH PRIVILEGES;
EOF

sudo systemctl stop mysqld
sudo systemctl set-environment MYSQLD_OPTS="--skip-grant-tables"
sudo systemctl start mysqld

mysql -sfu root -e "UPDATE mysql.user SET authentication_string = PASSWORD('root11'), password_expired = 'N' WHERE User = 'root' AND Host = 'localhost'"
echo "Changed root password at: "$(date +"%T") >> $log

sudo systemctl stop mysqld
sudo systemctl unset-environment MYSQLD_OPTS
sudo systemctl start mysqld

mysql -sfu root -proot11< "dbsetup.sql"
echo "Applied dbsetup.sql script at: "$(date +"%T") >> $log



chown -R $user$:$user$ /home/$user$
sudo su - $user$
echo "Completed deploy at: "$(date +"%T") >> $log