package com.msaggik.tenthlessonmultiplayerandpermission;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // поля
    private ActivityResultLauncher<String[]> storagePermissionLauncher; // поле результата активности параметризованного массивом разрешений
    private final String [] PERMISSIONS_ARRAY = new String[] {android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}; // массив разрешений
    private final String DATA_SD = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/Н.А.Римский-Корсаков - Полёт шмеля.mp3"; // путь к аудио-файлу
    private FloatingActionButton fabPlayPause, fabBack, fabForward; // кнопки управления воспроизведением
    private MediaPlayer mediaPlayer; // поле медиа-плеера

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // привязка полей к разметке
        fabPlayPause = findViewById(R.id.fab_play_pause);
        fabBack = findViewById(R.id.fab_back);
        fabForward = findViewById(R.id.fab_forward);

        // метод регистрации разрешения
        registerPermission();
        // метод проверки наличия разрешения
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // считывание аудио-файла с SD-карты
        readTrackExternalStorage();

        // обработка нажатия кнопок
        fabPlayPause.setOnClickListener(listener);
        fabBack.setOnClickListener(listener);
        fabForward.setOnClickListener(listener);
    }

    // регистрация разрешения
    private void registerPermission() {
        storagePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                result -> result.forEach(
                        (permission, granted) -> {
                            switch (permission){
                                case Manifest.permission.READ_EXTERNAL_STORAGE:
                                    createToast(granted ? "Разрешение доступа к аудиофайлам дано": "Доступ к аудиофайлам запрещён");
                                    break;
                                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                                    createToast(granted ? "Разрешение для сохранения аудиофайлов дано": "Доступ для сохранения аудиофайлов запрещён");
                                    break;
                            }
                        }));
    }


    private void createToast(String text){
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    // метод проверки разрешения и вывод диалогового окна запроса разрешения
    private void checkPermission(String permission) {
        // проверка разрешения
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Дано разрешение на чтение аудио-файлов с SD-карты", Toast.LENGTH_SHORT).show();
        } else { // запрос массива разрешений
            storagePermissionLauncher.launch(PERMISSIONS_ARRAY);
        }
    }

    // метод считывания аудио-файла из SD карты
    private void readTrackExternalStorage() {
        mediaPlayer = new MediaPlayer(); // создание объекта медиа-плеера
        try {
            mediaPlayer.setDataSource(DATA_SD); // указание источника аудио
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // подключение аудио-менеджера
            mediaPlayer.prepare(); // ассинхронная подготовка плеера к проигрыванию
        } catch (IOException exception) {
            Toast.makeText(this, "Запрашиваемого аудио-файла на SD-карте не нашлось", Toast.LENGTH_SHORT).show();
        }
    }

    // создадим один слушатель на все кнопки
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.fab_play_pause:
                    checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);

                    // код старта и паузы
                    if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                        // назначение кнопке картинки паузы
                        fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                        mediaPlayer.start();
                    } else if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        // назначение кнопке картинки воспроизведения
                        fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                        mediaPlayer.pause();
                    }
                    break;
                case R.id.fab_back:
                    // код перемотки назад
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000); // перемотка назад на 5 секунд
                    }
                    break;
                case R.id.fab_forward:
                    // код перемотки вперёд
                    if (mediaPlayer != null) {
                        mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000); // перемотка вперёд на 5 секунд
                    }
                    break;
            }
        }
    };

    // метод очистки занятой аудио-плеером памяти
    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release(); // очистка памяти
            mediaPlayer = null; // обнуление объекта аудио-плеера
        }
    }

    @Override
    protected void onDestroy() {
        releasePlayer(); // очистка памяти прошлого воспроизведения
        super.onDestroy();
    }
}