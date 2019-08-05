import xgboost as xgb
# read in data
dtrain = xgb.DMatrix('breakout-training-train.csv?format=csv&label_column=0')
dtest = xgb.DMatrix('breakout-training-test.csv?format=csv&label_column=0')
dvalid = xgb.DMatrix('short-test.csv?format=csv&label_column=0')
# specify parameters via map
param = {'max_depth':13, 'eta':0.3, 'gamma':10, 'verbosity':2, 'objective':'reg:linear' }
num_round = 30
evallist = [(dtest, 'eval'), (dtrain, 'train')]
bst = xgb.train(param, dtrain, num_round, evallist)
# make prediction
preds = bst.predict(dvalid)
# the model can now be saved with 
# bst.save_model('0001.model')