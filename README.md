# breakout
A java project that plays Breakout game in VICE C-64 emulator

Directory "parent" is a Maven project with the following modules (subprojects):
* **main**: the main program (DistributedBreakout) and its common datastructures
* **simple**: a non-distributed version of the main program (Breakout)
* **aws**: AWS dependencies, such as integration to DynamoDB and SQS
* **lamdba**: AWS lambda handler
* **predictor**: XGBoost based AI
