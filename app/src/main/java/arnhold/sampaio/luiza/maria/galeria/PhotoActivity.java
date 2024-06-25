package arnhold.sampaio.luiza.maria.galeria;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;

public class PhotoActivity extends AppCompatActivity {

    String photoPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_photo);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.tbPhoto); // obtendo o elemento "tbMain"
        setSupportActionBar(toolbar); // definindo ele como ActionBar padrão da tela

        ActionBar actionBar = getSupportActionBar(); // obtendo a actionBar
        actionBar.setDisplayHomeAsUpEnabled(true); // habilitando o botao de voltar

        Intent i = getIntent(); // obtendo a foto que foi enviada atraves do metodo "startPhotoActivity"
        photoPath = i.getStringExtra("photo_path");

        Bitmap bitmap = Util.getBitmap(photoPath); // carregando a foto em um bitmap
        ImageView imPhoto = findViewById(R.id.imPhoto);
        imPhoto.setImageBitmap(bitmap); // setando o bitmap no imageView

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_tb, menu);
        return true;
    }

    // esse metodo só será executado caso seja selecionado um item do ToolBar
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.opShare) {
            sharePhoto(); // vai compartilhar caso tenh sido selecionado uma foto
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // metodo para cpmpartiilhar a foto
    void sharePhoto() {
        Uri photoUri = FileProvider.getUriForFile(PhotoActivity.this, "arnhold.sampaio.luiza.maria.galeria.fileprovider", new File(photoPath)); // gerando URI para a foto
        Intent i = new Intent(Intent.ACTION_SEND); // criamos um Intent implícito (quero enviar algo para qualquer app que me aceite!)
        i.putExtra(Intent.EXTRA_STREAM, photoUri); // esse eh o arquivo que quero compartilhar
        i.setType("image/jpeg"); // e esse eh o seu tipo
        startActivity(i); // execucao do intent
    }
}