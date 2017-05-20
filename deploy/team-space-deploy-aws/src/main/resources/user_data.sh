#! /bin/bash
yum -y install wget
yum -y install zip unzip
yum -y install dos2unix
mkdir /home/deployer
cd /home/deployer
wget https://s3.amazonaws.com/aws-cli/awscli-bundle.zip
unzip awscli-bundle.zip
./awscli-bundle/install -i /usr/local/aws -b /usr/local/bin/aws
./awscli-bundle/install -b ~/bin/
aws s3 cp s3://$bucketName$/$tarFileName$.tar.gz $tarFileName$.tar.gz
tar -zxvf $tarFileName$.tar.gz
cd $tarFileName$
sudo su
chmod +x setup.sh
dos2unix setup.sh
./setup.sh