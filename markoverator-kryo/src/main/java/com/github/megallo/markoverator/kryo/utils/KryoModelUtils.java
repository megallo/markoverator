package com.github.megallo.markoverator.kryo.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.megallo.markoverator.bigrammer.BigramModel;

import java.io.InputStream;
import java.io.OutputStream;

public class KryoModelUtils {

    /**
     * Write out the model object to a stream, e.g. a file on disk
     * @param model a BigramModel object, like the one built by buildModel()
     * @param outputStream a writeable stream, such as java.io.FileOutputStream
     */
    public static void saveModel(BigramModel model, OutputStream outputStream) {
        if (model == null) {
            throw new RuntimeException("Refusing to write empty model.");
        }
        Kryo kryo = new Kryo();
        Output output = new Output(outputStream);
        kryo.writeObject(output, model);
        output.close();
    }

    /**
     * Load a model object from a stream, e.g. a file on disk
     * @param inputStream readable stream we can read a previously built model from
     * @return a BigramModel that's ready to load into Bigrammer
     */
    public static BigramModel loadModel(InputStream inputStream) {
        Kryo kryo = new Kryo();
        Input input = new Input(inputStream);
        BigramModel model = kryo.readObject(input, BigramModel.class);
        input.close();

        return model;
    }
}
