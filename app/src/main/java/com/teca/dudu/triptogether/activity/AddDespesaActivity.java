package com.teca.dudu.triptogether.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teca.dudu.triptogether.R;
import com.teca.dudu.triptogether.adapter.CategoriasAdapter;
import com.teca.dudu.triptogether.adapter.QuemUsouAdapter;
import com.teca.dudu.triptogether.dao.DespesaDao;
import com.teca.dudu.triptogether.dao.ItemDespesaDao;
import com.teca.dudu.triptogether.dao.UsuarioDao;
import com.teca.dudu.triptogether.dao.UsuarioViagemDao;
import com.teca.dudu.triptogether.model.Despesa;
import com.teca.dudu.triptogether.model.ItemDespesa;
import com.teca.dudu.triptogether.model.Usuario;
import com.teca.dudu.triptogether.util.Categoria;
import com.teca.dudu.triptogether.util.InicializaCategorias;

import java.util.ArrayList;

public class AddDespesaActivity extends AppCompatActivity {
    ItemDespesaDao novaDespesa;

    TextView descTV, valorTV;
    Button addDespesaBtn, quemUsouBtn, categoriasBtn, moedasBtn;
    ListView listViewQuemUsou;
    String moeda;
    ArrayList<Usuario> usuarios;
    UsuarioDao usuarioDao;
    UsuarioViagemDao usuarioViagemDao;
    ArrayList<UsuarioPagante> usuariosPagantes;
    int id_usuario,id_viagem, numCategoria;

    int[] ids_usuarios;
    boolean[] checkUsuarios;
    boolean visible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_despesa);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.ada_titulo_str);

        usuariosPagantes = new ArrayList<UsuarioPagante>();

        usuarioDao = new UsuarioDao(this);

        //pega id do usuario logado

        SharedPreferences sharedPref = this.getSharedPreferences(
                getString(R.string.ID_file_key), Context.MODE_PRIVATE);
        id_usuario = sharedPref.getInt(getString(R.string.ID_file_key),-1);


        if(id_usuario != -1){//pega a idviagem atual do usuario logado
            usuarioViagemDao = new UsuarioViagemDao(this);
            sharedPref = getSharedPreferences(
                    getString(R.string.ID_VIAGEM_file_key), Context.MODE_PRIVATE);
            id_viagem = sharedPref.getInt(getString(R.string.ID_VIAGEM_file_key),-1);

        }

        usuarios  = usuarioDao.listaUsuariosDeUmaViagem(id_viagem);

        ids_usuarios = new int[usuarios.size()];
        for(int i=0;i<usuarios.size();i++){
            ids_usuarios[i] = usuarios.get(i).get_id();
        }

        descTV = (TextView)findViewById(R.id.desc_text);
        valorTV = (TextView)findViewById(R.id.valor_text);
        categoriasBtn =  (Button) findViewById(R.id.categorias_btn);

        addDespesaBtn = (Button) findViewById(R.id.add_despesa_btn);
        quemUsouBtn = (Button) findViewById(R.id.quemusou_btn);
        moedasBtn = (Button) findViewById(R.id.moeda_btn);
        moeda = "$";


        listViewQuemUsou = (ListView) findViewById(R.id.listview_quemusou);
        final ArrayList<String > moedas = new ArrayList<String>();
        moedas.add("$ - USD");
        moedas.add("R$ - BRL");
        moedas.add("£ - GBP");
        moedas.add("$ - ARS");
        moedasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder moedasDialog = new AlertDialog.Builder(v.getContext())
                        .setTitle(R.string.ada_moeda_str);
                moedasDialog.setAdapter(new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_list_item_1, moedas), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        moeda = moedas.get(which).split(" ")[0];
                        moedasBtn.setText(moeda);

                    }
                });
                moedasDialog.show();

            }
        });



        final ArrayList<Categoria> categorias = new InicializaCategorias(getApplication()).getCategorias();
        categoriasBtn.setText(categorias.get(0).getNomeCategoria());

        categoriasBtn.setCompoundDrawablesWithIntrinsicBounds(categorias.get(0).getIconeCategoria(), 0, 0, 0);
        final CategoriasAdapter adapter = new CategoriasAdapter(this, categorias);


        categoriasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder categoriasDialog = new AlertDialog.Builder(v.getContext())
                        .setTitle(R.string.categorias_nome_str);
                categoriasDialog.setAdapter(adapter, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        categoriasBtn.setText(categorias.get(which).getNomeCategoria());
                        numCategoria = categorias.get(which).getNumCategoria();
                        categoriasBtn.setCompoundDrawablesWithIntrinsicBounds(categorias.get(which).getIconeCategoria(), 0, 0, 0);
                    }
                });
                categoriasDialog.show();
            }
        });

        addDespesaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cria o item da despesa em si com o valor e seus atributos e lança no bd pelo ItemDespesaDao
                novaDespesa = new ItemDespesaDao(v.getContext());
                String descricao = descTV.getText().toString();
                String valorStr = valorTV.getText().toString();
                if(!(descricao.matches("") || valorStr.matches(""))) {//checa se os campos nao estao em branco
                    //add item despesa a tabela
                    Float valor = Float.valueOf(valorTV.getText().toString());
                    //cria a relacao de usuario com despesa na tabela DespesaDao
                    setValorPagoQuemUsou();
                    boolean camposValidosValorPago = validaCamposValorPagoQuemUsou();
                    if(camposValidosValorPago){

                        ItemDespesa despesa = new ItemDespesa(null, moeda , descTV.getText().toString(), numCategoria
                                , null, valor, id_viagem);
                        int id_itemdespesa = (int) novaDespesa.salvarItemDespesa(despesa);
                        criaDespesaDao(id_itemdespesa, v);

                        Intent intentmain = new Intent(AddDespesaActivity.this, MainActivity.class);
                        startActivity(intentmain);
                    } else {
                        Toast.makeText(v.getContext(), R.string.ada_toast1_str,
                                Toast.LENGTH_SHORT).show();
                    }


                } else{
                    Toast.makeText(v.getContext(), R.string.ada_toast2_str, Toast.LENGTH_SHORT).show();
                }

            }


            //valores dos textos dos editText de cada item do listview
            public void setValorPagoQuemUsou(){

                View itemV;
                EditText et;
                for (int i = 0; i <listViewQuemUsou.getCount(); i++) {
                    itemV = listViewQuemUsou.getChildAt(i);
                    et = (EditText) itemV.findViewById(R.id.itemquemusou_edittxt);
                    usuariosPagantes.get(i).setValorPago(et.getText().toString());
                }
            }
            //testa se a soma dos valores de entrada nos itens quemusou são iguais ao valor total da despesa
            public boolean validaCamposValorPagoQuemUsou(){
                float acm = 0;
                for(int i=0;i<usuariosPagantes.size();i++){
                    Float valor = new Float(usuariosPagantes.get(i).getValorPago());
                    acm += valor;

                }
                Float valorTotal = new Float(valorTV.getText().toString());
                return acm == valorTotal;
            }
            //abre o DAO de despesa para inserir os valores pagos e devidos
            //de acordo com cada usuarioPagante
            public void criaDespesaDao(int id_itemdespesa, View v){
                Float valorTotal = new Float(valorTV.getText().toString());
                for(int i=0;i<usuariosPagantes.size();i++){
                    Float valorUsuario = new Float(usuariosPagantes.get(i).getValorPago());
                    Despesa despesa = new Despesa(valorTotal/usuariosPagantes.size(),
                            valorUsuario, id_itemdespesa,
                            usuariosPagantes.get(i).getId_usuario(),
                            id_viagem);
                    DespesaDao despesaDao = new DespesaDao(v.getContext());
                    despesaDao.salvarDespesa(despesa);
                }
            }
        });

        quemUsouBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog(getString(R.string.ada_quemusou_str), v);
            }
        });
    }



    private void createDialog(String quem, View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        CharSequence[] itens = new CharSequence[usuarios.size()];
        checkUsuarios = new boolean[usuarios.size()];
        for(int i =0;i<usuarios.size();i++){
            itens[i] = usuarios.get(i).getNome().toString();
        }

        builder.setTitle(quem)
                .setMultiChoiceItems(itens, checkUsuarios, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkUsuarios[which] = isChecked;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for(int i=0; i<usuarios.size();i++){
                            if(checkUsuarios[i]) {
                                usuariosPagantes.add( new UsuarioPagante(ids_usuarios[i])); //null pointer exception
                                visible = true;
                            }
                        }
                        if(visible)
                            displayQuemUsou();
                    }
                })
                .show();

    }
    public void displayQuemUsou(){
        ArrayList<Usuario> quemUsou = new ArrayList<Usuario>();
        for(int i=0; i<usuarios.size();i++){
            if(checkUsuarios[i]){
                quemUsou.add(usuarioDao.buscarUsuarioPorId(ids_usuarios[i]));

            }
        }
        QuemUsouAdapter adapter = new QuemUsouAdapter(this, quemUsou);
        listViewQuemUsou.setAdapter(adapter);
        //listViewQuemUsou.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usuarioViagemDao.close();
        usuarioDao.close();
        novaDespesa.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
class UsuarioPagante{
    private String valorPago;
    private int id_usuario;

    public UsuarioPagante(int id_usuario, String valorPago) {
        this.id_usuario = id_usuario;
        this.valorPago = valorPago;
    }

    public UsuarioPagante(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public String getValorPago() {
        return valorPago;
    }

    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    public void setValorPago(String valorPago) {
        this.valorPago = valorPago;
    }
}