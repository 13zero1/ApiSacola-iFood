package me.dio.sacola.repository;

import me.dio.sacola.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository     // essa anotação cria um vinculo dessa interface com o banco de dados
public interface ProdutoRepository extends JpaRepository<Produto,Long> {
}
