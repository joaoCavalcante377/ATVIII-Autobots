package com.autobots.automanager.servicos;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.autobots.automanager.entidades.*;
import com.autobots.automanager.enumeracoes.PerfilUsuario;
import com.autobots.automanager.repositorios.*;
import com.autobots.automanager.requisicoes.VeiculoRequisicao;

/** Veiculos pertencem a um usuario com perfil CLIENTE; o proprietario e definido no cadastro. */
@Service
@Transactional
public class VeiculoServico {
    private final VeiculoRepositorio repositorio;
    private final VendaRepositorio vendas;
    private final UsuarioServico usuarioServico;

    public VeiculoServico(VeiculoRepositorio repositorio, VendaRepositorio vendas, UsuarioServico usuarioServico) {
        this.repositorio = repositorio;
        this.vendas = vendas;
        this.usuarioServico = usuarioServico;
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listar(Long proprietarioId) {
        if (proprietarioId != null) usuarioServico.buscar(proprietarioId);
        return repositorio.filtrar(proprietarioId);
    }

    @Transactional(readOnly = true)
    public Veiculo buscar(long id) {
        return repositorio.findById(id).orElseThrow(() -> Erros.naoEncontrado("Veiculo nao encontrado"));
    }

    public Veiculo criar(long proprietarioId, VeiculoRequisicao dados) {
        Usuario proprietario = usuarioServico.buscar(proprietarioId);
        if (!proprietario.getPerfis().contains(PerfilUsuario.CLIENTE)) {
            throw Erros.invalido("O proprietario do veiculo precisa ter o perfil CLIENTE");
        }
        Veiculo veiculo = new Veiculo();
        veiculo.setProprietario(proprietario);
        return repositorio.saveAndFlush(aplicar(veiculo, dados, 0L));
    }

    public Veiculo atualizar(long id, VeiculoRequisicao dados) {
        return repositorio.saveAndFlush(aplicar(buscar(id), dados, id));
    }

    private Veiculo aplicar(Veiculo veiculo, VeiculoRequisicao dados, long id) {
        String placa = Conversor.placa(dados.getPlaca());
        if (repositorio.existsByPlacaAndIdNot(placa, id)) throw Erros.conflito("Ja existe veiculo com a placa " + placa);
        veiculo.setPlaca(placa);
        veiculo.setModelo(dados.getModelo().strip());
        veiculo.setTipo(dados.getTipo());
        return veiculo;
    }

    public void excluir(long id) {
        Veiculo veiculo = buscar(id);
        if (vendas.existsByVeiculoId(id)) throw Erros.conflito("O veiculo possui vendas registradas e nao pode ser excluido");
        repositorio.delete(veiculo);
        repositorio.flush();
    }
}
