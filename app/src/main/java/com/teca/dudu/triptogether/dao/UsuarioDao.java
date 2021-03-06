package com.teca.dudu.triptogether.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.teca.dudu.triptogether.model.Usuario;

import java.util.ArrayList;


/**
 * Created by tales on 28/08/16.
 */
public class UsuarioDao {
    private DataBaseHelper dataBaseHelper;
    private SQLiteDatabase database;
    private Context context;
    public UsuarioDao(Context context){
        dataBaseHelper = new DataBaseHelper(context);
        this.context=context;
    }

    private SQLiteDatabase getDatabase(){
        if(database == null){
            database = dataBaseHelper.getWritableDatabase();
        }

        return database;
    }

    public void close(){
        dataBaseHelper.close();
        database = null;
    }

    private Usuario criaUsuario(Cursor cursor){
        Usuario model = new Usuario(

                cursor.getInt(cursor.getColumnIndex(DataBaseHelper.Usuario._ID)),
                cursor.getString(cursor.getColumnIndex(DataBaseHelper.Usuario.NOME)),
                cursor.getString(cursor.getColumnIndex(DataBaseHelper.Usuario.NICKNAME)),
                cursor.getString(cursor.getColumnIndex(DataBaseHelper.Usuario.EMAIL)),
                cursor.getString(cursor.getColumnIndex(DataBaseHelper.Usuario.SENHA)),
                cursor.getBlob(cursor.getColumnIndex(DataBaseHelper.Usuario.IMG_PERFIL))
        );

        return model;
    }

    public ArrayList<Usuario> listaUsuarios(){
        Cursor cursor = getDatabase().query(DataBaseHelper.Usuario.TABELA,
                DataBaseHelper.Usuario.COLUNAS,null,null,null,null,null);
        ArrayList<Usuario> usuarios = new ArrayList<Usuario>();

        while (cursor.moveToNext()){
            Usuario model = criaUsuario(cursor);
            usuarios.add(model);
        }
        cursor.close();
        return usuarios;
    }
    public ArrayList<Usuario> listaUsuariosDeUmItem(int id_viagem, int id_itemDespesa){
        Cursor cursor = getDatabase().rawQuery("SELECT _id,nome,ImgPerfil,nickname,email,senha " +
                        "FROM Usuario JOIN Despesa on Usuario._id = Despesa.ID_Usuario " +
                        "WHERE Despesa.ID_ItemDespesa = ? and Despesa.ID_Viagem = ?;",
                new String[]{Integer.toString(id_itemDespesa),Integer.toString(id_viagem)});

        ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
        while (cursor.moveToNext()){
            Usuario model = criaUsuario(cursor);
            usuarios.add(model);
        }
        cursor.close();
        return usuarios;
    }
    public ArrayList<Usuario> listaUsuariosDeUmaViagem(int id_viagem){
        Cursor cursor = getDatabase().rawQuery(
                "select Usuario._id as _id,Usuario.nome as nome,Usuario.nickname as nickname," +
                "Usuario.email as email,Usuario.senha as senha, Usuario.ImgPerfil as ImgPerfil " +
                "from Usuario join UsuarioViagem on Usuario._id = UsuarioViagem.ID_Usuario " +
                "where UsuarioViagem.ID_Viagem = ?",// 3 joins
                new String[]{Integer.toString(id_viagem)});
        ArrayList<Usuario> usuarios = new ArrayList<Usuario>();

        while (cursor.moveToNext()){
            Usuario model = criaUsuario(cursor);
            usuarios.add(model);
        }
        cursor.close();
        return usuarios;
    }

    public int loginUsuario(String email,String senha){
        Cursor cursor = getDatabase().query(DataBaseHelper.Usuario.TABELA,DataBaseHelper.Usuario.COLUNAS,
                "email = ? AND senha = ?",new String[]{email,senha},null,null,null);

        if(cursor.moveToNext()){
            Usuario model = criaUsuario(cursor);
            return model.get_id();
        }

        return -1;
    }

    public long salvarUsuario(Usuario usuario){
        ContentValues valores = new ContentValues();
        valores.put(DataBaseHelper.Usuario.NOME,usuario.getNome());
        valores.put(DataBaseHelper.Usuario.NICKNAME,usuario.getNickname());
        valores.put(DataBaseHelper.Usuario.EMAIL,usuario.getEmail());
        valores.put(DataBaseHelper.Usuario.SENHA,usuario.getSenha());
       /* int bytes = usuario.getImgPerfil().getByteCount();
        //or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
        //int bytes = b.getWidth()*b.getHeight()*4;

        ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
        usuario.getImgPerfil().copyPixelsToBuffer(buffer); //Move the byte data to the buffer

        byte[] array = buffer.array();*/

        valores.put(DataBaseHelper.Usuario.IMG_PERFIL,usuario.getImgPerfil());

        if(usuario.get_id() != null){
            return getDatabase().update(DataBaseHelper.Usuario.TABELA,valores, "_id = ?",
                    new String[]{usuario.get_id().toString()});
        }
        return getDatabase().insert(DataBaseHelper.Usuario.TABELA,null,valores);
    }

    public boolean removerUsuario(int id){
        return getDatabase().delete(DataBaseHelper.Usuario.TABELA,"_id = ?",
                new String[]{Integer.toString(id)}) > 0;
    }

    public Usuario buscarUsuarioPorEmail(String email){
        Cursor cursor = getDatabase().query(DataBaseHelper.Usuario.TABELA,DataBaseHelper.Usuario.COLUNAS,"Email = ?",
                new String[]{email},null,null,null);
        if (cursor.moveToNext()){
            Usuario model = criaUsuario(cursor);
            cursor.close();
            return model;
        }
        cursor.close();
        return null;
    }

    public Usuario buscarUsuarioPorId(int id){
        Cursor cursor = getDatabase().query(DataBaseHelper.Usuario.TABELA,DataBaseHelper.Usuario.COLUNAS, "_id = ?",
                new String[]{Integer.toString(id)},null,null,null);


        if (cursor.moveToNext()){
            Usuario model = criaUsuario(cursor);
            cursor.close();
            return model;
        }

        return null;
    }

    
}
