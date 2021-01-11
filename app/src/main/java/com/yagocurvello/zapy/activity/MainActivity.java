package com.yagocurvello.zapy.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.yagocurvello.zapy.R;
import com.yagocurvello.zapy.config.ConfigFirebase;
import com.yagocurvello.zapy.fragment.ContatosFragment;
import com.yagocurvello.zapy.fragment.ConversasFragment;

public class MainActivity extends AppCompatActivity {

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    //RequestCodes para permissoes
    private static final int SELECAO_CAMERA = 100;
    private static final int SELECAO_GALERIA = 200;
    private static final int PERMISSOES = 300;

    private FirebaseAuth firebaseAuth;
    private MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Zapy");
        setSupportActionBar(toolbar);

        //Configs iniciais
        firebaseAuth = ConfigFirebase.getFirebaseAutenticacao();

        //Configurar Abas
        final FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(),
                FragmentPagerItems.with(this)
                .add("Conversas",ConversasFragment.class)
                .add("Contatos",ContatosFragment.class)
                .create()
        );
        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.viewPagerTab);
        viewPagerTab.setViewPager(viewPager);

        //Configuração para boto de pesquisa
        searchView = findViewById(R.id.materialSearchPrincipal);
        //Listener para quando abrir/fechar caixa de pesquisa
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                ConversasFragment fragment = (ConversasFragment) adapter.getPage(0);
                fragment.recarregarConversas();
            }
        });

        //Listener para caixa de texto (alteração no texto ou quando clicar no pesquisa)
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                //Verificar se está pesquisando no fragment Conversa ou Contato a partir da tab ativa
                switch (viewPager.getCurrentItem()){
                    case 0:
                        ConversasFragment conversasFragment = (ConversasFragment) adapter.getPage(0);
                        if (newText != null && !newText.isEmpty()){
                            conversasFragment.pesquisarConversas(newText.toLowerCase());
                        }else {
                            conversasFragment.recarregarConversas();
                        }

                        break;

                    case 1:
                        ContatosFragment contatosFragment = (ContatosFragment) adapter.getPage(1);
                        if (newText != null && !newText.isEmpty()){
                            contatosFragment.pesquisarContatos(newText.toLowerCase());
                        }else {
                            contatosFragment.recarregarContatos();
                        }
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //Configurar botao de pesquisa
        MenuItem menuItem = menu.findItem(R.id.menuPesquisa);
        searchView.setMenuItem(menuItem);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuConfig :
                abrirConfiguracoes();
                break;

            case R.id.menuSair :
                deslogarUsuario();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void deslogarUsuario(){
        try {
            firebaseAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void abrirConfiguracoes(){
        startActivity(new Intent(MainActivity.this, ConfiguracoesActivity.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissoes negadas");
        builder.setMessage("Para utilizar esse app, é necessario que sejam concedidas todas as permissões pedidas");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface,int i) {
                finish();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}