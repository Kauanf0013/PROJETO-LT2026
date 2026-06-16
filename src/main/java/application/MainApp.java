package application;

import dao.ClienteDAO;
import dao.EntregadorDAO;
import dao.PedidoDAO;
import dao.ProdutoDAO;

import db.DB;
import db.DbIntegrityExeption;

import entities.Cliente;
import entities.Entregador;
import entities.Pedido;
import entities.Produto;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.util.List;

public class MainApp extends Application {

    private ClienteDAO clienteDao = new ClienteDAO();
    private ProdutoDAO produtoDao = new ProdutoDAO();
    private EntregadorDAO entregadorDao = new EntregadorDAO();
    private PedidoDAO pedidoDao = new PedidoDAO();

    private Label lblStatus;
    private Label lblConexao;

    private TextField txtClienteId;
    private TextField txtClienteNome;
    private TextField txtClienteTelefone;
    private TextField txtClienteEmail;
    private TableView<Cliente> tabelaClientes;

    private TextField txtProdutoId;
    private TextField txtProdutoNome;
    private TextField txtProdutoPreco;
    private TableView<Produto> tabelaProdutos;

    private TextField txtEntregadorId;
    private TextField txtEntregadorNome;
    private TextField txtEntregadorTelefone;
    private TableView<Entregador> tabelaEntregadores;

    private TextField txtPedidoId;
    private ComboBox<Cliente> cbCliente;
    private ComboBox<Produto> cbProduto;
    private ComboBox<Entregador> cbEntregador;
    private TextField txtQuantidade;
    private ComboBox<String> cbStatus;
    private TableView<Pedido> tabelaPedidos;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Sistema de Delivery");

        lblStatus = new Label("Status: sistema iniciado.");
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");

        TabPane abas = new TabPane();

        Tab abaInicio = new Tab("Início", criarTelaInicio());
        Tab abaClientes = new Tab("Clientes", criarTelaClientes());
        Tab abaProdutos = new Tab("Produtos", criarTelaProdutos());
        Tab abaEntregadores = new Tab("Entregadores", criarTelaEntregadores());
        Tab abaPedidos = new Tab("Pedidos", criarTelaPedidos());

        abaInicio.setClosable(false);
        abaClientes.setClosable(false);
        abaProdutos.setClosable(false);
        abaEntregadores.setClosable(false);
        abaPedidos.setClosable(false);

        abas.getTabs().addAll(abaInicio, abaClientes, abaProdutos, abaEntregadores, abaPedidos);

        VBox root = new VBox(10, abas, lblStatus);
        root.setPadding(new Insets(10));

        Scene scene = new Scene(root, 900, 650);
        stage.setScene(scene);
        stage.show();

        testarConexao();
        atualizarListas();
    }

    private VBox criarTelaInicio() {
        Label titulo = new Label("Aplicativo Delivery");
        titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        Label texto = new Label("BEM VINDOS.");
        lblConexao = new Label("Testando conexão...");

        return new VBox(15, titulo, texto, lblConexao);
    }

    private VBox criarTelaClientes() {
        txtClienteId = new TextField();
        txtClienteId.setPromptText("ID automático");
        txtClienteId.setEditable(false);

        txtClienteNome = new TextField();
        txtClienteNome.setPromptText("Nome do cliente");

        txtClienteTelefone = new TextField();
        txtClienteTelefone.setPromptText("Telefone");

        txtClienteEmail = new TextField();
        txtClienteEmail.setPromptText("Email");

        tabelaClientes = new TableView<>();

        TableColumn<Cliente, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(x -> new SimpleIntegerProperty(x.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<Cliente, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getNome()));
        colNome.setPrefWidth(180);

        TableColumn<Cliente, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getTelefone()));
        colTelefone.setPrefWidth(140);

        TableColumn<Cliente, String> colEndereco = new TableColumn<>("Email");
        colEndereco.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getEmail()));
        colEndereco.setPrefWidth(280);

        tabelaClientes.getColumns().addAll(colId, colNome, colTelefone, colEndereco);
        tabelaClientes.setPrefHeight(300);

        tabelaClientes.setOnMouseClicked(e -> {
            Cliente c = tabelaClientes.getSelectionModel().getSelectedItem();
            if (c != null) {
                txtClienteId.setText(String.valueOf(c.getId()));
                txtClienteNome.setText(c.getNome());
                txtClienteTelefone.setText(c.getTelefone());
                txtClienteEmail.setText(c.getEmail());
            }
        });

        Button btnSalvar = new Button("Salvar");
        Button btnEditar = new Button("Editar");
        Button btnDeletar = new Button("Deletar");
        Button btnLimpar = new Button("Limpar");

        btnSalvar.setOnAction(e -> salvarCliente());
        btnEditar.setOnAction(e -> editarCliente());
        btnDeletar.setOnAction(e -> deletarCliente());
        btnLimpar.setOnAction(e -> limparCliente());

        HBox botoes = new HBox(10, btnSalvar, btnEditar, btnDeletar, btnLimpar);

        return new VBox(8,
                new Label("ID:"), txtClienteId,
                new Label("Nome:"), txtClienteNome,
                new Label("Telefone:"), txtClienteTelefone,
                new Label("Email:"), txtClienteEmail,
                botoes,
                tabelaClientes
        );
    }

    private VBox criarTelaProdutos() {
        txtProdutoId = new TextField();
        txtProdutoId.setPromptText("ID automático");
        txtProdutoId.setEditable(false);

        txtProdutoNome = new TextField();
        txtProdutoNome.setPromptText("Nome do produto");

        txtProdutoPreco = new TextField();
        txtProdutoPreco.setPromptText("Preço");

        tabelaProdutos = new TableView<>();

        TableColumn<Produto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(x -> new SimpleIntegerProperty(x.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<Produto, String> colNome = new TableColumn<>("Produto");
        colNome.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getNome()));
        colNome.setPrefWidth(250);

        TableColumn<Produto, Double> colPreco = new TableColumn<>("Preço");
        colPreco.setCellValueFactory(x -> new SimpleDoubleProperty(x.getValue().getPreco()).asObject());
        colPreco.setPrefWidth(120);

        tabelaProdutos.getColumns().addAll(colId, colNome, colPreco);
        tabelaProdutos.setPrefHeight(300);

        tabelaProdutos.setOnMouseClicked(e -> {
            Produto p = tabelaProdutos.getSelectionModel().getSelectedItem();
            if (p != null) {
                txtProdutoId.setText(String.valueOf(p.getId()));
                txtProdutoNome.setText(p.getNome());
                txtProdutoPreco.setText(String.valueOf(p.getPreco()));
            }
        });

        Button btnSalvar = new Button("Salvar");
        Button btnEditar = new Button("Editar");
        Button btnDeletar = new Button("Deletar");
        Button btnLimpar = new Button("Limpar");

        btnSalvar.setOnAction(e -> salvarProduto());
        btnEditar.setOnAction(e -> editarProduto());
        btnDeletar.setOnAction(e -> deletarProduto());
        btnLimpar.setOnAction(e -> limparProduto());

        HBox botoes = new HBox(10, btnSalvar, btnEditar, btnDeletar, btnLimpar);

        return new VBox(8,
                new Label("ID:"), txtProdutoId,
                new Label("Nome:"), txtProdutoNome,
                new Label("Preço:"), txtProdutoPreco,
                botoes,
                tabelaProdutos
        );
    }

    private VBox criarTelaEntregadores() {
        txtEntregadorId = new TextField();
        txtEntregadorId.setPromptText("ID automático");
        txtEntregadorId.setEditable(false);

        txtEntregadorNome = new TextField();
        txtEntregadorNome.setPromptText("Nome do entregador");

        txtEntregadorTelefone = new TextField();
        txtEntregadorTelefone.setPromptText("Telefone");

        tabelaEntregadores = new TableView<>();

        TableColumn<Entregador, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(x -> new SimpleIntegerProperty(x.getValue().getId()).asObject());
        colId.setPrefWidth(60);

        TableColumn<Entregador, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getNome()));
        colNome.setPrefWidth(250);

        TableColumn<Entregador, String> colTelefone = new TableColumn<>("Telefone");
        colTelefone.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getTelefone()));
        colTelefone.setPrefWidth(160);

        tabelaEntregadores.getColumns().addAll(colId, colNome, colTelefone);
        tabelaEntregadores.setPrefHeight(300);

        tabelaEntregadores.setOnMouseClicked(e -> {
            Entregador ent = tabelaEntregadores.getSelectionModel().getSelectedItem();
            if (ent != null) {
                txtEntregadorId.setText(String.valueOf(ent.getId()));
                txtEntregadorNome.setText(ent.getNome());
                txtEntregadorTelefone.setText(ent.getTelefone());
            }
        });

        Button btnSalvar = new Button("Salvar");
        Button btnEditar = new Button("Editar");
        Button btnDeletar = new Button("Deletar");
        Button btnLimpar = new Button("Limpar");

        btnSalvar.setOnAction(e -> salvarEntregador());
        btnEditar.setOnAction(e -> editarEntregador());
        btnDeletar.setOnAction(e -> deletarEntregador());
        btnLimpar.setOnAction(e -> limparEntregador());

        HBox botoes = new HBox(10, btnSalvar, btnEditar, btnDeletar, btnLimpar);

        return new VBox(8,
                new Label("ID:"), txtEntregadorId,
                new Label("Nome:"), txtEntregadorNome,
                new Label("Telefone:"), txtEntregadorTelefone,
                botoes,
                tabelaEntregadores
        );
    }

    private VBox criarTelaPedidos() {
        txtPedidoId = new TextField();
        txtPedidoId.setPromptText("ID automático");
        txtPedidoId.setEditable(false);

        cbCliente = new ComboBox<>();
        cbCliente.setPromptText("Cliente");

        cbProduto = new ComboBox<>();
        cbProduto.setPromptText("Produto");

        cbEntregador = new ComboBox<>();
        cbEntregador.setPromptText("Entregador");

        txtQuantidade = new TextField();
        txtQuantidade.setPromptText("Quantidade");

        cbStatus = new ComboBox<>();
        cbStatus.setPromptText("Status");
        cbStatus.getItems().addAll("aguardando", "em preparo", "em rota", "entregue", "cancelado");

        tabelaPedidos = new TableView<>();

        TableColumn<Pedido, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(x -> new SimpleIntegerProperty(x.getValue().getId()).asObject());
        colId.setPrefWidth(50);

        TableColumn<Pedido, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getClienteNome()));
        colCliente.setPrefWidth(150);

        TableColumn<Pedido, String> colProduto = new TableColumn<>("Produto");
        colProduto.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getProdutoNome()));
        colProduto.setPrefWidth(150);

        TableColumn<Pedido, String> colEntregador = new TableColumn<>("Entregador");
        colEntregador.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getEntregadorNome()));
        colEntregador.setPrefWidth(150);

        TableColumn<Pedido, Integer> colQtd = new TableColumn<>("Qtd");
        colQtd.setCellValueFactory(x -> new SimpleIntegerProperty(x.getValue().getQuantidade()).asObject());
        colQtd.setPrefWidth(70);

        TableColumn<Pedido, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(x -> new SimpleStringProperty(x.getValue().getStatus()));
        colStatus.setPrefWidth(130);

        tabelaPedidos.getColumns().addAll(colId, colCliente, colProduto, colEntregador, colQtd, colStatus);
        tabelaPedidos.setPrefHeight(250);

        tabelaPedidos.setOnMouseClicked(e -> {
            Pedido p = tabelaPedidos.getSelectionModel().getSelectedItem();
            if (p != null) {
                txtPedidoId.setText(String.valueOf(p.getId()));
                selecionarCliente(p.getClienteId());
                selecionarProduto(p.getProdutoId());
                selecionarEntregador(p.getEntregadorId());
                txtQuantidade.setText(String.valueOf(p.getQuantidade()));
                cbStatus.setValue(p.getStatus());
            }
        });

        Button btnSalvar = new Button("Salvar");
        Button btnEditar = new Button("Editar");
        Button btnDeletar = new Button("Deletar");
        Button btnLimpar = new Button("Limpar");

        btnSalvar.setOnAction(e -> salvarPedido());
        btnEditar.setOnAction(e -> editarPedido());
        btnDeletar.setOnAction(e -> deletarPedido());
        btnLimpar.setOnAction(e -> limparPedido());

        HBox botoes = new HBox(10, btnSalvar, btnEditar, btnDeletar, btnLimpar);

        return new VBox(8,
                new Label("ID:"), txtPedidoId,
                new Label("Cliente:"), cbCliente,
                new Label("Produto:"), cbProduto,
                new Label("Entregador:"), cbEntregador,
                new Label("Quantidade:"), txtQuantidade,
                new Label("Status:"), cbStatus,
                botoes,
                tabelaPedidos
        );
    }

    private void testarConexao() {
        try {
            Connection conn = DB.getConnection();
            lblConexao.setText("Conectado ao MySQL com sucesso.");
            lblStatus.setText("Status: banco conectado.");
        } catch (Exception e) {
            lblConexao.setText("Erro na conexão com o banco.");
            mostrarErro("Erro ao conectar: " + e.getMessage());
        }
    }

    private void atualizarListas() {
        try {
            List<Cliente> clientes = clienteDao.findAll();
            tabelaClientes.getItems().clear();
            tabelaClientes.getItems().addAll(clientes);
            cbCliente.getItems().clear();
            cbCliente.getItems().addAll(clientes);

            List<Produto> produtos = produtoDao.findAll();
            tabelaProdutos.getItems().clear();
            tabelaProdutos.getItems().addAll(produtos);
            cbProduto.getItems().clear();
            cbProduto.getItems().addAll(produtos);

            List<Entregador> entregadores = entregadorDao.findAll();
            tabelaEntregadores.getItems().clear();
            tabelaEntregadores.getItems().addAll(entregadores);
            cbEntregador.getItems().clear();
            cbEntregador.getItems().addAll(entregadores);

            List<Pedido> pedidos = pedidoDao.findAll();
            tabelaPedidos.getItems().clear();
            tabelaPedidos.getItems().addAll(pedidos);

        } catch (Exception e) {
            mostrarErro("Erro ao carregar listas: " + e.getMessage());
        }
    }

    private void salvarCliente() {
        try {
            if (txtClienteNome.getText().isBlank()) {
                mostrarErro("Informe o nome do cliente.");
                return;
            }
            Cliente c = new Cliente(null, txtClienteNome.getText(), txtClienteEmail.getText(), txtClienteTelefone.getText());
            clienteDao.insert(c);
            mostrarSucesso("Cliente salvo.");
            limparCliente();
            atualizarListas();
        } catch (Exception e) {
            mostrarErro("Erro ao salvar cliente: " + e.getMessage());
        }
    }

    private void editarCliente() {
        try {
            if (txtClienteId.getText().isBlank()) {
                mostrarErro("Selecione um cliente para editar.");
                return;
            }
            Cliente c = new Cliente(Integer.parseInt(txtClienteId.getText()), txtClienteNome.getText(), txtClienteEmail.getText(), txtClienteTelefone.getText());
            clienteDao.update(c);
            mostrarSucesso("Cliente atualizado.");
            limparCliente();
            atualizarListas();
        } catch (Exception e) {
            mostrarErro("Erro ao editar cliente: " + e.getMessage());
        }
    }

    private void deletarCliente() {
        try {
            if (txtClienteId.getText().isBlank()) {
                mostrarErro("Selecione um cliente para deletar.");
                return;
            }
            clienteDao.deleteById(Integer.parseInt(txtClienteId.getText()));
            mostrarSucesso("Cliente deletado.");
            limparCliente();
            atualizarListas();
        } catch (DbIntegrityExeption e) {
            mostrarErro("Não dá para deletar cliente que possui pedido.");
        } catch (Exception e) {
            mostrarErro("Erro ao deletar cliente: " + e.getMessage());
        }
    }

    private void salvarProduto() {
        try {
            if (txtProdutoNome.getText().isBlank() || txtProdutoPreco.getText().isBlank()) {
                mostrarErro("Informe nome e preço do produto.");
                return;
            }
            Produto p = new Produto(null, txtProdutoNome.getText(), Double.parseDouble(txtProdutoPreco.getText().replace(",", ".")));
            produtoDao.insert(p);
            mostrarSucesso("Produto salvo.");
            limparProduto();
            atualizarListas();
        } catch (Exception e) {
            mostrarErro("Erro ao salvar produto: " + e.getMessage());
        }
    }

    private void editarProduto() {
        try {
            if (txtProdutoId.getText().isBlank()) {
                mostrarErro("Selecione um produto para editar.");
                return;
            }
            Produto p = new Produto(Integer.parseInt(txtProdutoId.getText()), txtProdutoNome.getText(), Double.parseDouble(txtProdutoPreco.getText().replace(",", ".")));
            produtoDao.update(p);
            mostrarSucesso("Produto atualizado.");
            limparProduto();
            atualizarListas();
        } catch (Exception e) {
            mostrarErro("Erro ao editar produto: " + e.getMessage());
        }
    }

    private void deletarProduto() {
        try {
            if (txtProdutoId.getText().isBlank()) {
                mostrarErro("Selecione um produto para deletar.");
                return;
            }
            produtoDao.deleteById(Integer.parseInt(txtProdutoId.getText()));
            mostrarSucesso("Produto deletado.");
            limparProduto();
            atualizarListas();
        } catch (DbIntegrityExeption e) {
            mostrarErro("Não dá para deletar produto que possui pedido.");
        } catch (Exception e) {
            mostrarErro("Erro ao deletar produto: " + e.getMessage());
        }
    }

    private void salvarEntregador() {
        try {
            if (txtEntregadorNome.getText().isBlank()) {
                mostrarErro("Informe o nome do entregador.");
                return;
            }
            Entregador e = new Entregador(null, txtEntregadorNome.getText(), txtEntregadorTelefone.getText());
            entregadorDao.insert(e);
            mostrarSucesso("Entregador salvo.");
            limparEntregador();
            atualizarListas();
        } catch (Exception ex) {
            mostrarErro("Erro ao salvar entregador: " + ex.getMessage());
        }
    }

    private void editarEntregador() {
        try {
            if (txtEntregadorId.getText().isBlank()) {
                mostrarErro("Selecione um entregador para editar.");
                return;
            }
            Entregador e = new Entregador(Integer.parseInt(txtEntregadorId.getText()), txtEntregadorNome.getText(), txtEntregadorTelefone.getText());
            entregadorDao.update(e);
            mostrarSucesso("Entregador atualizado.");
            limparEntregador();
            atualizarListas();
        } catch (Exception ex) {
            mostrarErro("Erro ao editar entregador: " + ex.getMessage());
        }
    }

    private void deletarEntregador() {
        try {
            if (txtEntregadorId.getText().isBlank()) {
                mostrarErro("Selecione um entregador para deletar.");
                return;
            }
            entregadorDao.deleteById(Integer.parseInt(txtEntregadorId.getText()));
            mostrarSucesso("Entregador deletado.");
            limparEntregador();
            atualizarListas();
        } catch (DbIntegrityExeption e) {
            mostrarErro("Não dá para deletar entregador que possui pedido.");
        } catch (Exception e) {
            mostrarErro("Erro ao deletar entregador: " + e.getMessage());
        }
    }

    private void salvarPedido() {
        try {
            if (cbCliente.getValue() == null || cbProduto.getValue() == null || cbEntregador.getValue() == null || cbStatus.getValue() == null) {
                mostrarErro("Selecione cliente, produto, entregador e status.");
                return;
            }
            Pedido p = new Pedido();
            p.setClienteId(cbCliente.getValue().getId());
            p.setProdutoId(cbProduto.getValue().getId());
            p.setEntregadorId(cbEntregador.getValue().getId());
            p.setQuantidade(Integer.parseInt(txtQuantidade.getText()));
            p.setStatus(cbStatus.getValue());
            pedidoDao.insert(p);
            mostrarSucesso("Pedido salvo.");
            limparPedido();
            atualizarListas();
        } catch (Exception e) {
            mostrarErro("Erro ao salvar pedido: " + e.getMessage());
        }
    }

    private void editarPedido() {
        try {
            if (txtPedidoId.getText().isBlank()) {
                mostrarErro("Selecione um pedido para editar.");
                return;
            }
            if (cbCliente.getValue() == null || cbProduto.getValue() == null || cbEntregador.getValue() == null || cbStatus.getValue() == null) {
                mostrarErro("Selecione cliente, produto, entregador e status.");
                return;
            }
            Pedido p = new Pedido();
            p.setId(Integer.parseInt(txtPedidoId.getText()));
            p.setClienteId(cbCliente.getValue().getId());
            p.setProdutoId(cbProduto.getValue().getId());
            p.setEntregadorId(cbEntregador.getValue().getId());
            p.setQuantidade(Integer.parseInt(txtQuantidade.getText()));
            p.setStatus(cbStatus.getValue());
            pedidoDao.update(p);
            mostrarSucesso("Pedido atualizado.");
            limparPedido();
            atualizarListas();
        } catch (Exception e) {
            mostrarErro("Erro ao editar pedido: " + e.getMessage());
        }
    }

    private void deletarPedido() {
        try {
            if (txtPedidoId.getText().isBlank()) {
                mostrarErro("Selecione um pedido para deletar.");
                return;
            }
            pedidoDao.deleteById(Integer.parseInt(txtPedidoId.getText()));
            mostrarSucesso("Pedido deletado.");
            limparPedido();
            atualizarListas();
        } catch (Exception e) {
            mostrarErro("Erro ao deletar pedido: " + e.getMessage());
        }
    }

    private void limparCliente() {
        txtClienteId.clear();
        txtClienteNome.clear();
        txtClienteTelefone.clear();
        txtClienteEmail.clear();
        tabelaClientes.getSelectionModel().clearSelection();
    }

    private void limparProduto() {
        txtProdutoId.clear();
        txtProdutoNome.clear();
        txtProdutoPreco.clear();
        tabelaProdutos.getSelectionModel().clearSelection();
    }

    private void limparEntregador() {
        txtEntregadorId.clear();
        txtEntregadorNome.clear();
        txtEntregadorTelefone.clear();
        tabelaEntregadores.getSelectionModel().clearSelection();
    }

    private void limparPedido() {
        txtPedidoId.clear();
        cbCliente.setValue(null);
        cbProduto.setValue(null);
        cbEntregador.setValue(null);
        txtQuantidade.clear();
        cbStatus.setValue(null);
        tabelaPedidos.getSelectionModel().clearSelection();
    }

    private void selecionarCliente(Integer id) {
        for (Cliente c : cbCliente.getItems()) {
            if (c.getId().equals(id)) {
                cbCliente.setValue(c);
                break;
            }
        }
    }

    private void selecionarProduto(Integer id) {
        for (Produto p : cbProduto.getItems()) {
            if (p.getId().equals(id)) {
                cbProduto.setValue(p);
                break;
            }
        }
    }

    private void selecionarEntregador(Integer id) {
        for (Entregador e : cbEntregador.getItems()) {
            if (e.getId().equals(id)) {
                cbEntregador.setValue(e);
                break;
            }
        }
    }

    private void mostrarSucesso(String mensagem) {
        lblStatus.setText("Status: " + mensagem);
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
    }

    private void mostrarErro(String mensagem) {
        lblStatus.setText("Erro: " + mensagem);
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText("Ocorreu um problema");
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    public static void main(String[] args) {
        launch(args);
    }
}