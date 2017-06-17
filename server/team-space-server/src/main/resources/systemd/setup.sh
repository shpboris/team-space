#!/bin/bash

cp team-space.service /etc/systemd/system/
cp team-space /etc/sysconfig/
systemctl enable team-space.service
systemctl daemon-reload
sudo systemctl start team-space.service