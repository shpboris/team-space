cd .
CALL az group delete -y -n TS1-RESOURCE_GROUP
CALL az group create -n TS1-RESOURCE_GROUP -l eastus
CALL az group deployment create -g TS1-RESOURCE_GROUP --template-file public_instance.json --parameters @params.json
pause