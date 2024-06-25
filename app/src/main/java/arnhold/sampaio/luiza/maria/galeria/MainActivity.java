package arnhold.sampaio.luiza.maria.galeria;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import android.Manifest;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> photos = new ArrayList<>();
    MainAdapter mainAdapter;
    static int RESULT_TAKE_PICTURE = 1;
    String currentPhotoPath;
    static int RESULT_REQUEST_PERMISSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.tbMain); // obtendo o elemento "tbMain"
        setSupportActionBar(toolbar); // definindo ele como ActionBar padrão da tela

        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File[] files = dir.listFiles(); // lendo a lista de fotos
        for(int i = 0; i < files.length; i++) { // adicionando na lista de fotos
            photos.add(files[i].getAbsolutePath()); // adicionando na lista de fotos
        }
        //criação do mainAdapter
        mainAdapter = new MainAdapter(MainActivity.this, photos);
        //setando ele no RecycleView
        RecyclerView rvGallery = findViewById(R.id.rvGallery);
        rvGallery.setAdapter(mainAdapter);
        //calculando as colunas e definindo o numero maximo de linhas para exibição
        float w = getResources().getDimension(R.dimen.itemWidth);
        int numberOfColumns = Util.calculateNoOfColumns(MainActivity.this, w);
        // aqui vai exibir as fotos em grid
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this, numberOfColumns);
        rvGallery.setLayoutManager(gridLayoutManager);

        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);

        checkForPermissions(permissions);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater(); // inflador de menu
        inflater.inflate(R.menu.main_activity_tb, menu);
        return true;
    }

    // esse metodo só será executado caso seja selecionado um item do ToolBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opCamera) {
            dispatchTakePictureIntent(); // aqui eh para chamar a camera do celular
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // essa função eh chamada para quando o usuario clicar em uma foto, essa função resolve qual foto será aberta
    public void startPhotoActivity(String photoPath) {
        Intent i = new Intent(MainActivity.this, PhotoActivity.class);
        i.putExtra("photo_path", photoPath);
        startActivity(i);
    }

    // metodo para disparar o app da camera
    private void dispatchTakePictureIntent() {
        // vamos criar um arquivo vazio dentro da pasta Pictures
        File f = null;
        try {
            f = createImageFile(); // tentando criar o arquivo
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Não foi possível criar o arquivo", Toast.LENGTH_LONG).show(); // caso não seja possivel, essa mensagem eh exibida
            return;
        }

        currentPhotoPath = f.getAbsolutePath(); // guardando o local que a foto esta senod manipulada no momento

        if(f != null) {
            Uri fUri = FileProvider.getUriForFile(MainActivity.this, "arnhold.sampaio.luiza.maria.galeria.fileprovider", f); // endereco URI para o arquivo de foto
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // Intent para disparar a camera
            i.putExtra(MediaStore.EXTRA_OUTPUT, fUri); // passamos a URI para dentro do intent
            startActivityForResult(i, RESULT_TAKE_PICTURE); // a app da camera eh finalmente iniciada
        }
    }
    // funcao para criar o arquivo dentro da pasta
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()); // estaremos utilizando data e hora para organizar as fotos
        String imageFileName = "JPEG_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File f = File.createTempFile(imageFileName, ".jpg", storageDir);
        return f;
    }
    // esse metodo será somente retomado apos a iniciação da camera
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_TAKE_PICTURE) {
            if(resultCode == Activity.RESULT_OK) { // verifica se a foto foi tirada
                photos.add(currentPhotoPath); // caso sim, seu local eh adicionado na lista de fotos

                mainAdapter.notifyItemInserted(photos.size()-1); // notoficamos o mainAdapter sobre isso acima (atualiza o RecycleView)
            }
            // se a foto nao for tirada, deletamos o arquivo criado para ela
            else {
                File f = new File(currentPhotoPath);
                f.delete();
            }
        }
    }

    private void checkForPermissions(List<String> permissions) {
        List<String> permissionsNotGranted = new ArrayList<>();  // aceitamos uma lista de permissões como entrada

        for(String permission : permissions) { // vamos verificar cada permissao
            if( !hasPermission(permission)) { // caso nao tenha sido confirmada
                permissionsNotGranted.add(permission); // sera guarda em uma lista que mantem apenas as desse tipo
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(permissionsNotGranted.size() > 0) {
                requestPermissions(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]),RESULT_REQUEST_PERMISSION);
            }
        }
    }
    // metodo que verifica se uma determinada permissão já foi concedida ou nao
    private boolean hasPermission(String permission) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ActivityCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }
    // esse metodo so sera invocado após o usuário conceder ou não as permissões requisitadas
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        final List<String> permissionsRejected = new ArrayList<>();
        if(requestCode == RESULT_REQUEST_PERMISSION) {
            // verifica se cada permissao foi concedida pelo usuario ou nao
            for(String permission : permissions) {
                if(!hasPermission(permission)) {
                    permissionsRejected.add(permission);
                }
            }
        }
        // se ha alguma que não foi concedida e eh necessaria, o sistema dispara uma mensagem
        if(permissionsRejected.size() > 0) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if(shouldShowRequestPermissionRationale(permissionsRejected.get(0))) // vamos mostrar a mensagem
                {
                    new AlertDialog.Builder(MainActivity.this).
                            setMessage("Para usar essa app é preciso conceder essas permissões"). // mensagem disparada
                            setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                // vamos pedir novamente a permissao necessaria para o funcionamento do sistema
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    requestPermissions(permissionsRejected.toArray(new
                                            String[permissionsRejected.size()]), RESULT_REQUEST_PERMISSION);
                                }
                            }).create().show();
                }
            }
        }
    }
}

