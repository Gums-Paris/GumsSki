package fr.cjpapps.gumsski;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

    /*  Utilisé pour exécuter la tâche asynchrone "callable" qui se chargera par exemple de récupérer des infos sur Internet.
     *   repris de EpicPandaForce dans :
     *   https://stackoverflow.com/questions/58767733/android-asynctask-api-deprecating-in-android-11-what-are-the-alternatives
     *   Une différence : il est mis static avec un getInstance ; pas indispensable mais ça mange pas de pain */

    public class TaskRunner {
        private final ExecutorService executor = Executors.newSingleThreadExecutor(); // change according to your requirements
        private final Handler handler = new Handler(Looper.getMainLooper());
        static TaskRunner instance;

        static TaskRunner getInstance() {
            synchronized(TaskRunner.class) {
                if (instance == null) {
                    instance = new TaskRunner();
                }
                return instance;
            }
        }

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
