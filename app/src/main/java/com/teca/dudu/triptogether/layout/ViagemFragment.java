package com.teca.dudu.triptogether.layout;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.teca.dudu.triptogether.R;
import com.teca.dudu.triptogether.dao.UsuarioDao;
import com.teca.dudu.triptogether.dao.UsuarioViagemDao;
import com.teca.dudu.triptogether.dao.ViagemDao;
import com.teca.dudu.triptogether.model.Usuario;
import com.teca.dudu.triptogether.model.Viagem;
import com.teca.dudu.triptogether.util.DividasSolucao;

import java.util.ArrayList;

/**
 * Created by DUDU on 20/09/2016.
 */
public class ViagemFragment extends Fragment {

    public static final String ARG_PAGE = "ARG_PAGE";

    private int mPage;
    private int id_usuario = 0, id_viagem = 0;
    TextView txtNomeViagem, txtLocalViagem;
    ArrayList<Usuario> usuarios;
    Button sugestaoViagemBtn, finalizarViagemBtn;
    public ViagemFragment(){}
    public  ViagemFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ViagemFragment fragment = new ViagemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getArguments().getInt(ARG_PAGE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saveInstanceState){
        final View rootView = inflater.inflate(R.layout.fragment_viagem, container, false);

        //pega o ID do usuario logado
        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.ID_file_key),Context.MODE_PRIVATE);
        UsuarioViagemDao usuarioViagemDao = new UsuarioViagemDao(rootView.getContext());
        id_usuario = sharedPref.getInt(getString(R.string.ID_file_key),-1);

        sharedPref = getActivity().getSharedPreferences(
                getString(R.string.ID_VIAGEM_file_key), Context.MODE_PRIVATE);
        id_viagem = sharedPref.getInt(getString(R.string.ID_VIAGEM_file_key),-1);

        if(id_viagem != -1) {
            ViagemDao viagemDao = new ViagemDao(rootView.getContext());
            Viagem viagemAtual = viagemDao.buscarViagemPorId(id_viagem);

            txtNomeViagem = (TextView) rootView.findViewById(R.id.nome_viagem);
            txtNomeViagem.setText(viagemAtual.getNome());
            txtLocalViagem = (TextView) rootView.findViewById(R.id.local_viagem);
            txtLocalViagem.setText(viagemAtual.getLocal());
            sugestaoViagemBtn = (Button) rootView.findViewById(R.id.sugestao_btn);
            sugestaoViagemBtn.setText(R.string.fragv_sugestao_str);

            finalizarViagemBtn = (Button) rootView.findViewById(R.id.finalizar_btn);
            finalizarViagemBtn.setText(R.string.fragv_finalizar_str);

            UsuarioDao usuarioDao = new UsuarioDao(rootView.getContext());
            usuarios = new ArrayList<Usuario>();
            usuarios = usuarioDao.listaUsuariosDeUmaViagem(id_viagem);
            usuarioDao.close();
            sugestaoViagemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DividasSolucao dividasSolucao = new DividasSolucao(v.getContext(), usuarios, id_viagem);
                    ArrayList<String> sugestoes = dividasSolucao.getResultado();
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_list_item_1, sugestoes);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.fragv_sugestao_str)
                            //.setMessage("(teste)"+ sugestoes.get(0))
                            .setAdapter(adapter, null)
                            .setPositiveButton("OK", null);
                    dialog.show();
                }
            });
        }


        //ADAPTER SET TO LISTVIEW

        //ListAdapter adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, despesas);
        //setListAdapter(adapter);

        return rootView;

    }
}
