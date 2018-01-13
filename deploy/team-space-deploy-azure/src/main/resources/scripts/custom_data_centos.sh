#! /bin/bash

echo '$user$ ALL=(ALL:ALL) NOPASSWD:ALL' >> /etc/sudoers

log=/home/$user$/deployer.log
touch $log
echo "Started deploy at: "$(date) >> $log

domain=""
subscription=""
client=""
secret=""

while [ "$#" -gt 0 ]; do
  case "$1" in
    -domain) domain="$2"; shift 2;;
    -subscription) subscription="$2"; shift 2;;
    -client) client="$2"; shift 2;;
	-secret) secret="$2"; shift 2;;

    -*) echo "Unknown option: "$1 ", exiting at: "$(date) >> $log; exit 1;;
  esac
done

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

rpm -Uvh https://s3.amazonaws.com/aaronsilber/public/authbind-2.1.1-0.1.x86_64.rpm
touch /etc/authbind/byport/443
chown $user$:$user$ /etc/authbind/byport/443
chmod 755 /etc/authbind/byport/443
touch /etc/authbind/byport/80
chown $user$:$user$ /etc/authbind/byport/80
chmod 755 /etc/authbind/byport/80
echo "Completed authbind config at: "$(date +"%T") >> $log

cd /home/$user$

echo "Started JRE download at: "$(date +"%T") >> $log
wget --no-cookies --no-check-certificate --header "Cookie: gpw_e24=http%3A%2F%2Fwww.oracle.com%2F; oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jre-8u131-linux-x64.rpm"
echo "Completed JRE download at: "$(date +"%T") >> $log
yum -y localinstall jre-8u131-linux-x64.rpm
echo "Completed JRE install at: "$(date +"%T") >> $log

echo "Started Azure CLI install at: "$(date +"%T") >> $log
rpm --import https://packages.microsoft.com/keys/microsoft.asc
sh -c 'echo -e "[azure-cli]\nname=Azure CLI\nbaseurl=https://packages.microsoft.com/yumrepos/azure-cli\nenabled=1\ngpgcheck=1\ngpgkey=https://packages.microsoft.com/keys/microsoft.asc" > /etc/yum.repos.d/azure-cli.repo'
yum -y check-update
yum -y install azure-cli
echo "Completed Azure CLI install at: "$(date +"%T") >> $log

cp /var/lib/waagent/custom-script/download/0/$tarFileName$.tar.gz /home/$user$/
tar -zxvf $tarFileName$.tar.gz
cd $tarFileName$

chmod +x setup.sh
dos2unix setup.sh

chown -R $user$:$user$ /home/$user$

cat >azdetails <<EOF

domain="$domain"
subscription="$subscription"
client="$client"
secret="$secret"
EOF


echo "Completed app extraction, ready to run a setup.sh at: "$(date +"%T") >> $log
./setup.sh

netstat -ln | grep ":80 " 2>&1 > /dev/null
while [ $? -ne 0 ]
do
    sleep 1
    netstat -ln | grep ":80 " 2>&1 > /dev/null
done
echo "Started the application at: "$(date +"%T") >> $log