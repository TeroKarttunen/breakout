# breakout
A java project that plays Breakout game in VICE C-64 emulator

Directory "**parent**" is a Maven project with the following modules (subprojects):
* **main**: the main program (DistributedBreakout) and its common datastructures
* **simple**: a non-distributed version of the main program (Breakout)
* **aws**: AWS dependencies, such as integration to DynamoDB and SQS
* **lamdba**: AWS lambda handler
* **predictor**: XGBoost based AI

Directory "**Breakout**" is a C64Studio project for building the C-64 program (Breakout game).

Directory "**analysis**" contains interesting information on different game solutions.

Directory "**docker**" contains files for building a docker container for requestprocessor. This is a distributed component that uses AWS SQS queues for receiving move processing instructions from the main program (DistributedBreakout). Please note that the Dockerfile is located in the root directory to get around technical limitations of docker build.

Directory "**ai**" contains the best current-known xgboost model and a python script that can be used in training the AI model.
