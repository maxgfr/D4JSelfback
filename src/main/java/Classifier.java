import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.berkeley.Pair;
import org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import java.util.*;

/**
 * Created by maxime on 01-Jun-17.
 */
public class Classifier {

    private int seed;//123
    private double learningRate;//0.01
    private int iteration;
    /** Defines number of samples that going to be propagated through the network.*/
    private int batchSize;//500
    private int nbEpochs;
    private int numInputs;//3
    private int numOutputs;//6
    private int numHiddenNodes;//30
    private MultiLayerNetwork model;

    public Classifier (int seed, double learningRate, int iteration, int batchSize, int nEpochs, int numInputs, int numOutputs, int numHiddenNodes) {
        this.seed = seed;
        this.learningRate = learningRate;
        this.iteration = iteration;
        this.batchSize= batchSize;
        this.nbEpochs = nEpochs;
        this.numInputs= numInputs;
        this.numOutputs= numOutputs;
        this.numHiddenNodes= numHiddenNodes;
    }

    public Classifier (double learningRate, int iteration, int batchSize, int nEpochs, int numOutputs) {
        this.learningRate = learningRate;
        this.iteration = iteration;
        this.batchSize = batchSize;
        this.nbEpochs = nEpochs;
        this.numOutputs = numOutputs;
    }

    public MultiLayerNetwork getModel () {return model;}

    public void setModel (MultiLayerNetwork mln) {model = mln;}

    public void createLSTM () {

        System.out.println("We're starting to create the LSTM network");

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)    //Random number generator seed for improved repeatability. Optional.
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .iterations(iteration)
                .weightInit(WeightInit.XAVIER)
                .updater(Updater.NESTEROVS).momentum(0.9)
                .learningRate(learningRate)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)  //Not always required, but helps with this data set
                .gradientNormalizationThreshold(0.5)
                .regularization(true).l2(0.0001)
                .list()
                .layer(0, new GravesLSTM.Builder().activation(Activation.TANH).nIn(numInputs).nOut(numHiddenNodes).build())
                .layer(1, new RnnOutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                        .activation(Activation.SOFTMAX).nIn(numHiddenNodes).nOut(numOutputs).build())
                .pretrain(false)
                .backprop(true)
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        model = new MultiLayerNetwork(conf);

        model.init();

        model.setListeners(new ScoreIterationListener(1));

        System.out.println("We finished to create the LSTM network");

    }

    public void createCNN () {

        System.out.println("We're starting to create the CNN network");

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

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        model = new MultiLayerNetwork(conf);

        model.init();

        model.setListeners(new ScoreIterationListener(100));

        System.out.println("We're starting to create the CNN network");
    }

    public void createMyOwnCNN () {

        System.out.println("We're starting to create the CNN network");

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

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        model = new MultiLayerNetwork(conf);

        model.init();

        model.setListeners(new ScoreIterationListener(100));

        System.out.println("We're starting to create the CNN network");
    }

    public void trainLSTM (DataSetIterator iteratorTrain, DataSetIterator testData) {

        System.out.println("We're starting to train the LSTM network");

        dispModel();

        for (int i=1; i<nbEpochs+1; i++) {
            model.fit(iteratorTrain);
            System.out.println(i+" epoch(s) completed");
            Evaluation evaluation = model.evaluate(testData);
            System.out.println("Evaluation of model with Accuracy = "+evaluation.accuracy()+" and F1 = "+evaluation.f1());
            testData.reset();
        }

        System.out.println("We finished to train the LSTM network");
    }

    public void trainCNN (DataSetIterator dataIter, DataSetIterator dataTest){

        System.out.println("We're starting to train the CNN network");
        dispModel();

        for (int i=1; i<nbEpochs+1; i++) {
            model.fit(dataIter);
            System.out.println(i+" epoch(s) completed");

            System.out.println("Evaluate model....");
            Evaluation eval = new Evaluation(numOutputs);
            while(dataTest.hasNext()){
                DataSet ds = dataTest.next();
                INDArray output = model.output(ds.getFeatureMatrix(), false);
                eval.eval(ds.getLabels(), output);
            }
            System.out.println(eval.stats());
            dataTest.reset();
        }

        System.out.println("We finished to train the CNN network");

    }

    public void makePrediction(DataSetIterator it) {
        //evaluate the model on the test set
        System.out.println("Prediction is starting");
        List<Integer> list = new LinkedList<Integer>();
        while (it.hasNext()) {
            DataSet ds = it.next();
            int[] ls = model.predict(ds.getFeatureMatrix());
            for(int s : ls) {
                list.add(s);
            }
            System.out.println("Iterator has next...");
        }
        dispOccurence(list);
    }

    private void dispOccurence (List<Integer> myList) {
        int downstairs,jogging,sitting,standing,upstairs,walking;
        downstairs=jogging=sitting=standing=upstairs=walking = 0;
        for (Integer x : myList) {
            switch (x) {
                case 0:  downstairs++;
                    break;
                case 1:  jogging++;
                    break;
                case 2:  sitting++;
                    break;
                case 3:  standing++;
                    break;
                case 4:  upstairs++;
                    break;
                case 5: walking++;
                    break;
            }
        }
        System.out.println("Number of occurrence :" +
                "\nfor downstairs is "+downstairs+
                "\nfor jogging is "+jogging+
                "\nfor sitting is "+sitting+
                "\nfor standing is "+ standing+
                "\nfor upstairs is " +upstairs+
                "\nfor walking is " +walking);
    }

    //http://localhost:9000/train
    private void dispModel () {
        //Initialize the user interface backend
        UIServer uiServer = UIServer.getInstance();

        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        StatsStorage statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);

        //Then add the StatsListener to collect this information from the network, as it trains
        model.setListeners(new StatsListener(statsStorage));
    }

}

