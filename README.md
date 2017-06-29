# D4JSelfback
Activity recognition through Neural Network

==The data provides from 50 person from wrist.==

## Resssource directory :

Data folder contains 6 files in whuch each file contain 1 collumn per axis (x,y,z) and all of the data from the activity
Label folder contains 6 files in which we can found the label for each file in the data folder

The **batchsize** is 500

## Evaluation :

- I use the same data to make my evaluation and my training.

- Each model in directory ``model`` was trained with 100% of the data available.

I need to remake an other training with 80% of training data and 20% of testing data in order to have a better evealuation of the models


## FOR CNN : 

NetworkD4J_CNN500 is the model which corresponds to CNN in the model folder. It uses data and label folder 

### Configuration : 

* 500 epochs,
* 0.01 of learning rate,
* No normalization of the data input
* DataInput.java : **height of 1, width of 500 and depth of 3**

```java
    MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iteration)
                .activation(Activation.RELU)
                .learningRate(learningRate)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.RMSPROP).momentum(0.9)
                .list()
                .layer(0, new ConvolutionLayer.Builder(1,10) //depends height
                        .nIn(3)//depth
                        .nOut(150)
                        .stride(1,1)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX) //max pooling
                        .kernelSize(1,2)
                        .stride(1,2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(1,10)
                        .nIn(150)
                        .nOut(100)
                        .stride(1,1)
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(1,2)
                        .stride(1,2)
                        .build())
                .layer(4, new ConvolutionLayer.Builder(1,10)
                        .nIn(100)
                        .nOut(80)
                        .stride(1,1)
                        .build())
                .layer(5, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(1,2)
                        .stride(1,2)
                        .build())
                .layer(6, new ConvolutionLayer.Builder(1,10)
                        .nIn(80)
                        .nOut(60)
                        .stride(1,1)
                        .build())
                .layer(7, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(1,2)
                        .stride(1,2)
                        .build())
                .layer(8, new ConvolutionLayer.Builder(1,10)
                        .stride(1,1)
                        .nIn(60)
                        .nOut(40)
                        .build())
                .layer(9, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(1,2)
                        .stride(1,2)
                        .build())
                .layer(10, new DenseLayer.Builder() //fullyConnected
                        .nOut(900)
                        .activation(Activation.TANH)
                        .build())
                .layer(11, new DenseLayer.Builder()
                        .nOut(300)
                        .activation(Activation.TANH)
                        .dropOut(0.5)
                        .build())
                .layer(12, new OutputLayer.Builder()
                        .activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(numOutputs)
                        .build())
                .setInputType(InputType.convolutional(1, 500, 3))
                .backprop(true)
                .pretrain(false)
                .build();
```

### Scores at the latest epoch :
```
Accuracy:        0.9456
Precision:       0.9326
Recall:          0.9216
F1 Score:        0.9271
```
About labelisation of the examples :

```
Examples labeled as 0 classified by model as 0: 618 times
Examples labeled as 0 classified by model as 1: 2 times
Examples labeled as 0 classified by model as 3: 1 times
Examples labeled as 0 classified by model as 4: 50 times
Examples labeled as 0 classified by model as 5: 21 times
Examples labeled as 1 classified by model as 0: 2 times
Examples labeled as 1 classified by model as 1: 1929 times
Examples labeled as 1 classified by model as 2: 2 times
Examples labeled as 1 classified by model as 3: 7 times
Examples labeled as 1 classified by model as 4: 5 times
Examples labeled as 1 classified by model as 5: 8 times
Examples labeled as 2 classified by model as 2: 1509 times
Examples labeled as 2 classified by model as 3: 16 times
Examples labeled as 2 classified by model as 4: 1 times
Examples labeled as 3 classified by model as 0: 2 times
Examples labeled as 3 classified by model as 2: 28 times
Examples labeled as 3 classified by model as 3: 1504 times
Examples labeled as 3 classified by model as 4: 1 times
Examples labeled as 3 classified by model as 5: 4 times
Examples labeled as 4 classified by model as 0: 17 times
Examples labeled as 4 classified by model as 2: 2 times
Examples labeled as 4 classified by model as 3: 4 times
Examples labeled as 4 classified by model as 4: 563 times
Examples labeled as 4 classified by model as 5: 175 times
Examples labeled as 5 classified by model as 0: 10 times
Examples labeled as 5 classified by model as 2: 1 times
Examples labeled as 5 classified by model as 3: 2 times
Examples labeled as 5 classified by model as 4: 74 times
Examples labeled as 5 classified by model as 5: 1442 times
```

![alt text](https://github.com/maxgfr/D4JSelfback/blob/master/screen/CNN_500/Capture.PNG)


## FOR CNN + RNN : 

NetworkD4J_CNN+RNN500 is the model which corresponds to CNN in the model folder. It uses data and label folder 

### Configuration : 

* 500 epochs,
* 0.01 of learning rate,
* no normalization of the data input
* DataInput.java : **height of 1, width of 500 and depth of 3**

```java
MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iteration)
                .activation(Activation.RELU)
                .learningRate(learningRate)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(Updater.RMSPROP).momentum(0.9)
                .list()
                .layer(0, new ConvolutionLayer.Builder(1,10) //depends height
                        .nIn(3)//depth
                        .nOut(130)
                        .stride(1,1)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX) //max pooling
                        .kernelSize(1,2)
                        .stride(1,2)
                        .build())
                .layer(2, new ConvolutionLayer.Builder(1,10)
                        .nIn(130)//depth
                        .nOut(70)
                        .stride(1,1)
                        .build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX) //max pooling
                        .kernelSize(1,2)
                        .stride(1,2)
                        .build())
                .layer(4, new ConvolutionLayer.Builder(1,10) //depends height
                        .nIn(70)//depth
                        .nOut(40)
                        .stride(1,1)
                        .build())
                .layer(5, new DenseLayer.Builder()
                        .nOut(150)
                        .activation(Activation.TANH)
                        .dropOut(0.5)
                        .build())
                .layer(6, new GravesLSTM.Builder()
                        .nIn(150)
                        .nOut(50)
                        .build())
                .layer(7, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX)
                        .nIn(50)
                        .nOut(numOutputs)
                        .build())
                .setInputType(InputType.convolutional(1, 500, 3))
                .backprop(true)
                .pretrain(false)
                .build();
```

### Scores at the latest epoch :

```
Accuracy:        0.8781
Precision:       0.8375
Recall:          0.8331
F1 Score:        0.8353
```
About labelisation of the examples :

```
Examples labeled as 0 classified by model as 0: 565 times
Examples labeled as 0 classified by model as 1: 7 times
Examples labeled as 0 classified by model as 4: 91 times
Examples labeled as 0 classified by model as 5: 34 times
Examples labeled as 1 classified by model as 0: 7 times
Examples labeled as 1 classified by model as 1: 1920 times
Examples labeled as 1 classified by model as 2: 2 times
Examples labeled as 1 classified by model as 3: 6 times
Examples labeled as 1 classified by model as 4: 9 times
Examples labeled as 1 classified by model as 5: 7 times
Examples labeled as 2 classified by model as 0: 1 times
Examples labeled as 2 classified by model as 1: 1 times
Examples labeled as 2 classified by model as 2: 1498 times
Examples labeled as 2 classified by model as 3: 13 times
Examples labeled as 2 classified by model as 5: 9 times
Examples labeled as 3 classified by model as 0: 8 times
Examples labeled as 3 classified by model as 1: 2 times
Examples labeled as 3 classified by model as 2: 58 times
Examples labeled as 3 classified by model as 3: 1435 times
Examples labeled as 3 classified by model as 4: 3 times
Examples labeled as 3 classified by model as 5: 36 times
Examples labeled as 4 classified by model as 0: 59 times
Examples labeled as 4 classified by model as 2: 1 times
Examples labeled as 4 classified by model as 3: 5 times
Examples labeled as 4 classified by model as 4: 359 times
Examples labeled as 4 classified by model as 5: 335 times
Examples labeled as 5 classified by model as 0: 49 times
Examples labeled as 5 classified by model as 1: 1 times
Examples labeled as 5 classified by model as 2: 1 times
Examples labeled as 5 classified by model as 3: 5 times
Examples labeled as 5 classified by model as 4: 225 times
Examples labeled as 5 classified by model as 5: 1248 times
```

![alt text](https://github.com/maxgfr/D4JSelfback/blob/master/screen/CNN+RNN_500/Capture.PNG)
