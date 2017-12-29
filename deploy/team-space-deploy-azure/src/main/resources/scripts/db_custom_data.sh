#! /bin/bash

cd /home/$user$
echo '$user$ ALL=(ALL:ALL) NOPASSWD:ALL' >> /etc/sudoers

log=/home/$user$/deployer.log
touch $log
echo "Started deploy at: "$(date) >> $log

sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password password root11'
sudo debconf-set-selections <<< 'mysql-server mysql-server/root_password_again password root11'
sudo apt-get -y install mysql-server
echo "Completed MySQl install at: "$(date +"%T") >> $log

sed -ie 's/bind-address/#bind-address/g' /etc/mysql/mysql.conf.d/mysqld.cnf
service mysql restart

cat >dbsetup.sql <<EOF
uninstall plugin validate_password;
DELETE FROM mysql.user WHERE User='';
DROP DATABASE IF EXISTS test;
CREATE DATABASE $dbname$;
CREATE USER '$user$'@'localhost' IDENTIFIED BY '$pass$';
GRANT ALL PRIVILEGES ON * . * TO '$user$'@'localhost' IDENTIFIED BY '$pass$';
CREATE USER '$user$'@'%' IDENTIFIED BY '$pass$';
GRANT ALL PRIVILEGES ON * . * TO '$user$'@'%' IDENTIFIED BY '$pass$';
FLUSH PRIVILEGES;
EOF

mysql -sfu root -proot11< "dbsetup.sql"
echo "Applied dbsetup.sql script at: "$(date +"%T") >> $log

chown -R $user$:$user$ /home/$user$
echo "Completed deploy at: "$(date +"%T") >> $log