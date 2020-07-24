SETUP
---
1. Setup SAM locally

https://www.youtube.com/watch?v=bih5b3C1nqc

2. Debug lambda locally

https://www.youtube.com/watch?v=FINV-VmCXms

3. Setup DynamoDB locally

https://www.youtube.com/watch?v=vHeYz-C9hSk

Note: To connect sam local to dynamodb local, you must use this connection string
http://host.docker.internal:8000
https://stackoverflow.com/questions/48926260/connecting-aws-sam-local-with-dynamodb-in-docker

BUILD SAM
---
1.
`sam package --output-template-file deploy.yaml --s3-bucket s3namnvh`

Note: change bucket name to your own bucket

2. 
`sam deploy --template-file deploy.yaml --stack-name sam-app --capabilities CAPABILITY_IAM --region ap-northeast-1`

RUN LAMBDA FUNCTION
---
1. Comment out endpoint_url in app.py if you use aws
2. Change email in EmailHandler.py
3. Run test as normal
4. Check your email, there should be an email with 3 fortunes