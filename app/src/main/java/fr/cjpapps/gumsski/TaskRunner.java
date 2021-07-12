package fr.cjpapps.gumsski;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

    /*  Utilisé pour exécuter la tâche asynchrone "callable" qui se chargera par exemple de récupérer des infos sur Internet.
     *   repris de EpicPandaForce dans :
     *   https://stackoverflow.com/questions/58767733/android-asynctask-api-deprecating-in-android-11-what-are-the-alternatives */

    public class TaskRunner {
        private final Executor executor = Executors.newSingleThreadExecutor(); // change according to your requirements
        private final Handler handler = new Handler(Looper.getMainLooper());

        public interface Callback<R> {
            void onComplete(R result);
        }

        public <Q> void executeAsync(Callable<Q> callable, Callback<Q> callback) {
            executor.execute(() -> {
                Q result = null;
                try {
                    result = callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Q finalResult = result;
                handler.post(() -> {
                    callback.onComplete(finalResult);
                });
            });
        }
    }
