rem aws cloudformation create-stack --stack-name VPC-INSTANCES --template-body file://c:\aws\aws-raw-instances.json --parameters file://c:\aws\params.json --capabilities CAPABILITY_NAMED_IAM --region eu-central-1

aws cloudformation create-stack --stack-name VPC-INSTANCES --template-body file://./public_private_instances.json --parameters file://./params.json --capabilities CAPABILITY_NAMED_IAM --region eu-central-1
pause