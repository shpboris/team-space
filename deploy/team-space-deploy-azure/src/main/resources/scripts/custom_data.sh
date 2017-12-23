#! /bin/bash

cd /home/$user$

echo '$user$ ALL=(ALL:ALL) NOPASSWD:ALL' >> /etc/sudoers

log=/home/$user$/deployer.log
touch $log
echo "Started deploy at: "$(date) >> $log

apt-get install -y zip unzip
echo "Completed zip/unzip install at: "$(date +"%T") >> $log
sudo apt-get install -y dos2unix
echo "Completed dos2unix install at: "$(date +"%T") >> $log

sudo apt-get install authbind
touch /etc/authbind/byport/443
chown $user$:$user$ /etc/authbind/byport/443
chmod 755 /etc/authbind/byport/443
touch /etc/authbind/byport/80
chown $user$:$user$ /etc/authbind/byport/80
chmod 755 /etc/authbind/byport/80
echo "Completed authbind config at: "$(date +"%T") >> $log

echo "deb [arch=amd64] https://packages.microsoft.com/repos/azure-cli/ wheezy main" | \
     sudo tee /etc/apt/sources.list.d/azure-cli.list
sudo apt-key adv --keyserver packages.microsoft.com --recv-keys 52E16F86FEE04B979B07E28DB02C46DF417A0893
sudo apt-get install -y apt-transport-https
sudo apt-get update -y && sudo apt-get install -y azure-cli
echo "Completed azure cli install at: "$(date +"%T") >> $log

sudo apt-get install -y python-software-properties debconf-utils
sudo add-apt-repository -y ppa:webupd8team/java
sudo apt-get update
echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | sudo debconf-set-selections
sudo apt-get install -y oracle-java8-installer
echo "Completed JDK install at: "$(date +"%T") >> $log

cat >azdetails <<EOF

domain="$domain$"
subscription="$subscription$"
client="$client$"
secret="$secret$"
EOF

az storage blob download --container-name $container-name$ --name $tarFileName$.tar.gz --file $tarFileName$.tar.gz --connection-string '$storage-connection-string$'
echo "Completed app download from S3 at: "$(date +"%T") >> $log
tar -zxvf $tarFileName$.tar.gz
cd $tarFileName$

chmod +x setup.sh
dos2unix setup.sh
chown -R $user$:$user$ /home/$user$

echo "Completed app extraction, ready to run a setup.sh at: "$(date +"%T") >> $log
./setup.sh

netstat -ln | grep ":80 " 2>&1 > /dev/null
while [ $? -ne 0 ]
do
    sleep 1
    netstat -ln | grep ":80 " 2>&1 > /dev/null
done
echo "Started the application at: "$(date +"%T") >> $log




