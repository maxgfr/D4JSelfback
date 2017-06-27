import org.datavec.api.io.WritableConverter;
import org.datavec.api.io.labels.PathLabelGenerator;
import org.datavec.api.io.labels.PatternPathLabelGenerator;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.SequenceRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.records.reader.impl.csv.CSVSequenceRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.split.NumberedFileInputSplit;
import org.datavec.api.writable.Writable;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.datasets.datavec.SequenceRecordReaderDataSetIterator;
import org.deeplearning4j.datasets.iterator.BaseDatasetIterator;
import org.deeplearning4j.datasets.iterator.INDArrayDataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.iterator.CachingDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by maxime on 02-Jun-17.
 */
public class DataSetManager {

    /** Instance unique non préinitialisée */
    private static DataSetManager INSTANCE = null;
    /** Defines number of samples that going to be propagated through the network.*/
    private int batchSize;//500
    /** Classes : downstairs,jogging,etc.*/
    private int numClasses;//6
    /**3-4-5 Parameters for my own dataset iterator*/
    private int height;//1
    private int width;//500
    private int depth;//3

    /** Constructor private */
    private DataSetManager(int batchSize, int numClasses, int height, int width, int depth) {
        this.batchSize = batchSize;
        this.numClasses = numClasses;
        this.height = height;
        this.width  = width;
        this.depth = depth;
    }

    /** Singleton */
    public static synchronized DataSetManager getInstance(int batchSize, int numClasses, int height, int width, int depth) {
        if (INSTANCE == null) {
            INSTANCE = new DataSetManager(batchSize,numClasses,height,width,depth);
        }
        return INSTANCE;
    }

    public DataSetIterator createDataSetIteratorForLSTM (File fileData, File fileLabel) throws IOException, InterruptedException {

        SequenceRecordReader trainFeatures = new CSVSequenceRecordReader(1,",");
        trainFeatures.initialize(new NumberedFileInputSplit(fileData.getAbsolutePath() + "/%d.csv", 1, 6));

        SequenceRecordReader trainLabels = new CSVSequenceRecordReader();
        trainLabels.initialize(new NumberedFileInputSplit(fileLabel.getAbsolutePath() + "/%d.csv", 1, 6));

        DataSetIterator trainData = new SequenceRecordReaderDataSetIterator(trainFeatures, trainLabels, batchSize, numClasses,
                false, SequenceRecordReaderDataSetIterator.AlignmentMode.ALIGN_END);

        System.out.println("Normalizer");

        //Normalize the training data
        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(trainData);
        trainData.reset();
        trainData.setPreProcessor(normalizer);

        System.out.println("End fit normalizer");

        return trainData;

    }

    public DataSetIterator createMyOwnDataSetIterator (File fileData) throws IOException, InterruptedException {

        DataInput di = new DataInput(height,width,depth);

        INDArrayDataSetIterator iterator =  di.getDataSetIterator(fileData);

        System.out.println("Normalizer");

        DataNormalization normalizer = new NormalizerStandardize();
        normalizer.fit(iterator);
        iterator.setPreProcessor(normalizer);

        System.out.println("End Normalizer");

        return iterator;

    }

    public DataSetIterator createDataSetIteratorTest (File fileData) throws IOException, InterruptedException {

        DataInput dataInput = new DataInput(height,width,depth);

        INDArrayDataSetIterator iterator = dataInput.getDataSetIteratorTest(fileData);

        return  iterator;

    }

}
