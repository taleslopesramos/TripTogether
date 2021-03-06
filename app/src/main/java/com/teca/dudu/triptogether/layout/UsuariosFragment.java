package com.teca.dudu.triptogether.layout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.teca.dudu.triptogether.R;
import com.teca.dudu.triptogether.activity.AddIntegranteActivity;
import com.teca.dudu.triptogether.adapter.UsuariosAdapter;
import com.teca.dudu.triptogether.dao.UsuarioDao;
import com.teca.dudu.triptogether.model.Usuario;

import java.util.ArrayList;

public class UsuariosFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";
    UsuarioDao usuarioDao;
    ArrayList<Usuario> usuarios;


    public UsuariosFragment(){}
    public  UsuariosFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        UsuariosFragment fragment = new UsuariosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getArguments().getInt(ARG_PAGE);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saveInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_usuarios, container, false);


        SharedPreferences sharedPref = getActivity().getSharedPreferences(
                getString(R.string.ID_VIAGEM_file_key), Context.MODE_PRIVATE);

        int id_viagem = sharedPref.getInt(getString(R.string.ID_VIAGEM_file_key),-1);

        usuarioDao = new UsuarioDao(rootView.getContext());
        //ADAPTER SET TO LISTVIEW
        if(id_viagem != -1) {
            usuarios = new ArrayList<Usuario>();
            usuarios = usuarioDao.listaUsuariosDeUmaViagem(id_viagem);
            ListView listUsuarios = (ListView) rootView.findViewById(R.id.list_usuarios);
            if (usuarios.size() >= 1) {
                UsuariosAdapter adapter = new UsuariosAdapter(rootView.getContext(), usuarios);
                listUsuarios.setAdapter(adapter);
            }
        }

        usuarioDao.close();

        FloatingActionButton fbaddUsuario = (FloatingActionButton) rootView.findViewById(R.id.fabadd_usuario);

        fbaddUsuario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentaddintegrante = new Intent(view.getContext(), AddIntegranteActivity.class);
                startActivity(intentaddintegrante);
            }
        });
        return rootView;
    }
    /*
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Toast.makeText(getActivity(), getListView().getItemAtPosition(position).toString(),
                Toast.LENGTH_LONG).show();
    }
    */
}
