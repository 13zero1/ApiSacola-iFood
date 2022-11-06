package me.dio.sacola.service.impl;

import lombok.RequiredArgsConstructor;
import me.dio.sacola.enumeration.FormaPagamento;
import me.dio.sacola.model.Item;
import me.dio.sacola.model.Restaurante;
import me.dio.sacola.model.Sacola;
import me.dio.sacola.repository.ItemRepository;
import me.dio.sacola.repository.ProdutoRepository;
import me.dio.sacola.repository.SacolaRepository;
import me.dio.sacola.resource.dto.ItemDto;
import me.dio.sacola.service.SacolaService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service    // essa anotação informa ao spring que ela é uma classe de serviço.
@RequiredArgsConstructor    // Essa anotação cria um construtor por conta do SacolaRepository
public class SacolaServiceImpl implements SacolaService {

    private final SacolaRepository sacolaRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemRepository itemRepository;

    @Override
    public Item incluirItemNaSacola(ItemDto itemDto) {
        Sacola sacola = verSacola(itemDto.getIdSacola());

        if (sacola.isFechada()) {
            throw new RuntimeException("A sacola está fechada!");
        }

        Item itemParaSerInserido =
                Item.builder().quantidade(itemDto.getQuantidade()).sacola(sacola).produto(produtoRepository.findById(itemDto.getProdutoId()).orElseThrow(()
                    -> {
                        throw new RuntimeException("Essa produto nao existe!");
                    }
                ))
                .build();

        List<Item> itensDaSacola = sacola.getItens();
        if (itensDaSacola.isEmpty()) {
            itensDaSacola.add(itemParaSerInserido);

        } else {
            Restaurante restauranteAtual = itensDaSacola.get(0).getProduto().getRestaurante();
            Restaurante restauranteDoItemParaAdicionar = itemParaSerInserido.getProduto().getRestaurante();

            if (restauranteAtual.equals(restauranteDoItemParaAdicionar)) {
                itensDaSacola.add(itemParaSerInserido);
            } else {
                throw new RuntimeException("Não é possivel adicionar produto de estabelecimento diferente!");
            }

        }

        List<Double> valorDosItens = new ArrayList<>();

        for(Item itemDaSacola: itensDaSacola){
            double valorTotalItem =
                    itemDaSacola.getProduto().getValorUnitario() * itemDaSacola.getQuantidade();
            valorDosItens.add(valorTotalItem);
        }

        // LÓGICA PARA CALCULAR O VALOR TOTAL DA SACOLA
        Double valorTotalSacola = valorDosItens.stream()
            .mapToDouble(valorTotalDeCadaItem -> valorTotalDeCadaItem)
            .sum();

        sacola.setValorTotal(valorTotalSacola);

    sacolaRepository.save(sacola);
        return itemParaSerInserido;
    }

    @Override
    public Sacola verSacola(Long id) {
        return sacolaRepository.findById(id).orElseThrow(() -> {
            throw new RuntimeException("Essa sacola nao existe!");
        });
    }

    @Override
    public Sacola fecharSacola(Long id, int numeroFormaPagamento) {
        Sacola sacola = verSacola(id);

        // SE A SACOLA ESTIVER VAZIA...
        if (sacola.getItens().isEmpty()) {
            throw new RuntimeException("Inclua itens na sacola!");
        }

        // FAZENDO UM IF ELSE TERNARIO...
        FormaPagamento formaPagamento = numeroFormaPagamento == 0 ? FormaPagamento.DINHEIRO : FormaPagamento.MAQUINETA;
        sacola.setFormaPagamento(formaPagamento);
        sacola.setFechada(true);
        return sacolaRepository.save(sacola);

    }
}
