#!/bin/bash

cat dbdetails >> team-space
sed -ie 's/DB_URL="/DB_URL="-Ddatabase.url=/g' team-space
sed -ie 's/DB_USER="/DB_USER="-Ddatabase.user=/g' team-space
sed -ie 's/DB_PASSWORD="/DB_PASSWORD="-Ddatabase.password=/g' team-space
sed -ie 's/DB_DRIVER="/DB_DRIVER="-Ddatabase.driver=/g' team-space

cp team-space.service /etc/systemd/system/
cp team-space /etc/sysconfig/
systemctl enable team-space.service
systemctl daemon-reload
sudo systemctl start team-space.service