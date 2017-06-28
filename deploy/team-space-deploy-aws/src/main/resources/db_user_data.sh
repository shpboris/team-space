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
#sudo service mysqld start
echo "Started MySQl at: "$(date +"%T") >> $log


cat >dbsetup.sql <<EOF
uninstall plugin validate_password;
DELETE FROM mysql.user WHERE User='';
DROP DATABASE IF EXISTS test;
CREATE DATABASE teamspace;
CREATE USER '$user$'@'localhost' IDENTIFIED BY '$pass$';
GRANT ALL PRIVILEGES ON * . * TO '$user$'@'%' IDENTIFIED BY '$pass$';
FLUSH PRIVILEGES;
EOF

#https://dba.stackexchange.com/questions/62521/why-cant-i-find-my-databases-from-mysql-on-linux
#sudo systemctl stop mysqld
#mysql -u ts -h ip-10-0-1-183.eu-central-1.compute.internal -p
#mysql -u USERNAME -pPASSWORD -h REMOTE_SERVER_IP DB_NAME
#GRANT ALL PRIVILEGES ON * . * TO 'ts'@'%' IDENTIFIED BY 'ts111';
#GRANT ALL PRIVILEGES ON *.* TO 'ts'@'%' IDENTIFIED BY 'ts111' WITH GRANT OPTION;
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