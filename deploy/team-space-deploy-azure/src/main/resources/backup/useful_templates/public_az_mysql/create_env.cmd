cd .
CALL az group delete -y -n TS2-RESOURCE_GROUP
CALL az group create -n TS2-RESOURCE_GROUP -l eastus
CALL az group deployment create -g TS2-RESOURCE_GROUP --template-file public_az_mysql.json --parameters @params.json
pause