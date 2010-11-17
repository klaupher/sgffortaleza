    create table SGF.TB_ABASTECIMENTO (
        CODSOLABASTECIMENTO int4 not null,
        DTAUTORIZACAO timestamp,
        JUSTIFICATIVAATEND varchar(255),
        KM_ATENDIMENTO int8,
        STATUS int4,
        CODUSUARIOAUT int4,
        CODTIPOCOMBUSTIVEL int4 not null,
        CODMOTORISTA int4 not null,
        CODPOSTO int4 not null,
        CODTIPOSERVICO int4 not null,
        CODVEICULO int4 not null,
        primary key (CODSOLABASTECIMENTO)
    );

    create table SGF.TB_AREA (
        CODAREA  serial not null,
        DESCAREA varchar(255),
        GEOMAREA oid not null,
        primary key (CODAREA)
    );

    create table SGF.TB_ATENDABASTECIMENTO (
        CODATENDABASTECIMENTO int4 not null,
        DATA timestamp,
        HORA_ATENDIMENTO timestamp,
        JUSTIFICATIVA varchar(100),
        LITRO_ABASTECIDO float8,
        KM_ATENDIMENTO int8,
        STATUS int4,
        CODSOLABASTECIMENTO int4 not null,
        CODBOMBA int4,
        CODUSUARIO int4,
        primary key (CODATENDABASTECIMENTO)
    );

    create table SGF.TB_CADPESSOA (
        CODPESSOA int4 not null,
        CPF varchar(255),
        MATRICULA numeric(19, 2),
        NOME varchar(255),
        ATIVO varchar(255),
        status_rh varchar(255),
        TPPESSOA int4,
        COD_UA varchar(255),
        primary key (CODPESSOA)
    );

    create table SGF.TB_CADVEICULO (
        CODVEICULO int4 not null,
        ANOFAB int4,
        ANOMODELO int4,
        CHASSI varchar(30),
        COMBUSTIVEL varchar(50),
        COR varchar(20),
        DT_CADASTRO timestamp,
        ESTACIONAMENTO varchar(255),
        KMIMPLANTACAO int4,
        KM_ATUAL int8,
        COD_PATRIMONIO varchar(255),
        NUMTOMBAMENTO int4,
        OBJETIVO varchar(255),
        PLACA varchar(7) not null,
        PROPRIEDADE varchar(150),
        RENAVAM varchar(30),
        SERIE varchar(30),
        STATUS int4 not null,
        TEMSEGURO int4,
        VALORAQUISICAO float4,
        CODESPECIE int4,
        CODMODELO int4,
        COD_UA_ASI varchar(255),
        primary key (CODVEICULO)
    );

    create table SGF.TB_ESPECIE (
        CODESPECIE int4 not null,
        DESCESPECIE varchar(150) not null,
        primary key (CODESPECIE)
    );

    create table SGF.TB_GRUPOUSUARIO (
        codgrupo int4,
        CODPESSOAUSUARIO int4 not null,
        primary key (CODPESSOAUSUARIO)
    );

    create table SGF.TB_INFRACAO (
        CODINFRACAO int4 not null,
        INFRACAO varchar(150) not null,
        GRAVIDADE varchar(150) not null,
        PESO int4,
        PONTOS int4,
        VALOR float8,
        primary key (CODINFRACAO)
    );

    create table SGF.TB_LOGUSUARIO (
        ID  serial not null,
        DATE_LOGIN timestamp,
        DATE_LOGOUT timestamp,
        IP_CON varchar(255),
        OBSERVACAO varchar(250),
        ID_USUARIO int4 not null,
        primary key (ID)
    );

    create table SGF.TB_MARCA (
        CODMARCA int4 not null,
        DESCMARCA varchar(50) not null,
        primary key (CODMARCA)
    );

    create table SGF.TB_MODELO (
        CODMODELO int4 not null,
        DESCMODELO varchar(120) not null,
        CODMARCA int4,
        primary key (CODMODELO)
    );

    create table SGF.TB_MULTA (
        CODMULTA int4 not null,
        DATACADASTRO timestamp not null,
        DATAINFRACAO timestamp,
        CODINFRACAO int4,
        CODPESSOAMOTORISTA int4,
        CODUSUARIO int4,
        CODVEICULO int4,
        primary key (CODMULTA)
    );

    create table SGF.TB_NUMEROMOTOR (
        CODNUMMOTOR int4 not null,
        DATACADASTRO timestamp not null,
        JUSTIFICATIVA varchar(250) not null,
        NUMMOTOR varchar(50) not null,
        CODUSUARIO int4,
        CODVEICULO int4,
        primary key (CODNUMMOTOR)
    );

    create table SGF.TB_OFICINA (
        CODOFICINA int4 not null,
        ENDERECO varchar(150) not null,
        NOME varchar(80) not null,
        RESPONSAVEL varchar(100),
        primary key (CODOFICINA)
    );

    create table SGF.TB_PAGE (
        CODPAGE  serial not null,
        BEANNAME varchar(255) not null,
        DESCPAGE varchar(150) not null,
        NOMEPAGE varchar(50) not null,
        URLPAGE varchar(255) not null,
        primary key (CODPAGE)
    );

    create table SGF.TB_PARAMETRO (
        ID_PARAMETRO  serial not null,
        DESCRICAO varchar(255),
        NOME varchar(255),
        TIPO varchar(255),
        VALOR varchar(255),
        primary key (ID_PARAMETRO)
    );

    create table SGF.TB_PESSOAUSUARIO (
        CODPESSOAUSUARIO int4 not null,
        autoriza bool,
        logado bool,
        LOGIN varchar(30) not null,
        SENHA varchar(90) not null,
        ATIVO varchar(255),
        CODPESSOA int4 not null,
        CODPOSTO int4,
        primary key (CODPESSOAUSUARIO)
    );

    create table SGF.TB_PONTO (
        CODPONTO  serial not null,
        DESCPONTO varchar(255),
        GEOMPONTO oid not null,
        primary key (CODPONTO)
    );

    create table SGF.TB_REGISTROVEICULOS (
        dtretorno timestamp,
        dtsaida timestamp,
        kmretorno int8,
        kmsaida int8,
        statusregistro int4,
        codpessoamotorista int4,
        codpessoausuario int4,
        CODSOLVEICULO int4 not null,
        primary key (CODSOLVEICULO)
    );

    create table SGF.TB_REQUISICAOITEM (
        CODREQITEM  serial not null,
        QTDE int4 not null,
        VALORTOT float4,
        VALORUNT float4 not null,
        CODREQMANUTENCAO int4,
        CODTIPOSERVICO int4,
        primary key (CODREQITEM)
    );

    create table SGF.TB_REQUISICAOMANUTENCAO (
        CODREQMANUTENCAO  serial not null,
        DATAFIM timestamp,
        DATAINI timestamp,
        DATA_RETORNO timestamp,
        DATA_SAIDA timestamp,
        MECANICO varchar(255) not null,
        STATUS int4,
        CODOFICINA int4,
        CODUSUARIO int4,
        CODVEICULO int4,
        primary key (CODREQMANUTENCAO)
    );

    create table SGF.TB_SOLTROCALUBRIFICANTE (
        CODSOLTROCALUB int4 not null,
        DTAUTORIZACAO timestamp,
        QUANT float8,
        STATUS int4,
        CODPESSOAMOTORISTA int4 not null,
        CODPOSTO int4 not null,
        CODTIPOSERVICO int4 not null,
        CODUSUARIOAUT int4,
        CODVEICULO int4 not null,
        primary key (CODSOLTROCALUB)
    );

    create table SGF.TB_SOLVEICULOS (
        CODSOLVEICULO int4 not null,
        DATAFIM timestamp,
        DATAINI timestamp,
        DESTINO varchar(255),
        JUSTIFICATIVA varchar(255),
        JUSTIFICATIVA_NEG varchar(255),
        JUSTIFICATIVA_RETORNO varchar(255),
        STATUSSOL int4 not null,
        CODUSUARIOAUT int4,
        CODUSUARIOSOL int4,
        CODVEICULO int4,
        CODVEICULOAUT int4,
        primary key (CODSOLVEICULO)
    );

    create table SGF.TB_TRANSMISSAO (
        CODTRANSMISSAO  bigserial not null,
        DAT_TRANSMISSAO timestamp not null,
        DISTANCIA_PONTOPROX float4,
        GEOM oid not null,
        IGNICAO bool,
        ODOMETRO float4,
        TEMPERATURA float4,
        CODVEICULO int4,
        VELOCIDADE float4,
        CODPONTO int4,
        primary key (CODTRANSMISSAO)
    );

    create table SGF.TB_UA (
        COD_UA varchar(255) not null,
        NM_UA varchar(255),
        FLAG int4,
        COD_UG varchar(255),
        primary key (COD_UA)
    );

    create table SGF.TB_ULTIMATRANSMISSAO (
        DAT_TRANSMISSAO timestamp,
        DISTANCIA_PONTOPROX float4,
        GEOM oid,
        IGNICAO bool,
        ODOMETRO float4,
        TEMPERATURA float4,
        VELOCIDADE float4,
        CODPONTOPROX int4,
        CODVEICULO int4 not null,
        primary key (CODVEICULO)
    );

    create table SGF.TB_VEICULO_AREA (
        CODAREA int4 not null,
        CODVEICULO int4 not null
    );

    create table SGF.tb_bomba (
        codbomba int4 not null,
        numero int4 not null,
        CODTIPOCOMBUSTIVEL int4 not null,
        CODPOSTO int4 not null,
        primary key (codbomba)
    );

    create table SGF.tb_cotaabastecimento (
        codcotaabastecimento int4 not null,
        COTA float8,
        COTA_DISPONIVEL_MES float8,
        CODTIPOSERVICO int4,
        CODVEICULO int4,
        primary key (codcotaabastecimento)
    );

    create table SGF.tb_diariobomba (
        coddiariobomba int4 not null,
        data timestamp,
        hora_fim timestamp,
        hora_ini timestamp,
        status int4,
        vlfinal float4,
        vlinicia float4,
        zerada bool,
        codbomba int4,
        codusuario int4,
        primary key (coddiariobomba)
    );

    create table SGF.tb_grupo (
        CODGRUPO int4 not null,
        AUTORIDADEGRUPO varchar(255),
        NOMEGRUPO varchar(255) not null,
        primary key (CODGRUPO)
    );

    create table SGF.tb_grupopage (
        codgrupo int4 not null,
        codpage int4 not null,
        primary key (codgrupo, codpage)
    );

    create table SGF.tb_grupopermissao (
        codgrupo int4 not null,
        codpermissao int4 not null
    );

    create table SGF.tb_permissao (
        CODPERMISSAO int4 not null,
        DESCPERMISSAO varchar(150) not null,
        NOMEPERMISSAO varchar(50) not null,
        primary key (CODPERMISSAO)
    );

    create table SGF.tb_pessoamotorista (
        CODPESSOAMOTORISTA int4 not null,
        ATIVO varchar(255),
        CATEGORIA varchar(255),
        CNH varchar(255),
        DATAVALIDADE timestamp,
        PONTOS_CNH int4 not null,
        CODPESSOA int4,
        primary key (CODPESSOAMOTORISTA)
    );

    create table SGF.tb_posto (
        CODPOSTO int4 not null,
        NOMEPOSTO varchar(255),
        primary key (CODPOSTO)
    );

    create table SGF.tb_postoservico (
        codpostoservico int4 not null,
        CODPOSTO int4,
        CODTIPOCOMBUSTIVEL int4,
        CODTIPOSERVICO int4,
        primary key (codpostoservico)
    );

    create table SGF.tb_postoservicoveiculo (
        codpostoservicoveiculo int4 not null,
        CODPOSTOSERVICO int4,
        CODVEICULO int4,
        primary key (codpostoservicoveiculo)
    );

    create table SGF.tb_tipocombustivel (
        codtipocombustivel int4 not null,
        descservico varchar(255),
        primary key (codtipocombustivel)
    );

    create table SGF.tb_tiposervico (
        CODTIPOSERVICO int4 not null,
        descservico varchar(255),
        manutencao int4,
        primary key (CODTIPOSERVICO)
    );

    create table SGF.tb_ug (
        cod_ug varchar(255) not null,
        nm_ug varchar(255),
        flag int4,
        primary key (cod_ug)
    );

    create table SGF.TB_ATENDTROCALUB (
        DTATENDIMENTO timestamp,
        JUSTIFICATIVA varchar(255),
        KMPROXTROCA int4,
        KMTROCAANTERIOR int4,
        KMTROCAATUAL int4,
        CODUSUARIOATEND int4,
        CODSOLTROCALUB int4 not null,
        primary key (CODSOLTROCALUB)
    );

    alter table SGF.TB_ABASTECIMENTO 
        add constraint FK8C04C7B6E4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.TB_ABASTECIMENTO 
        add constraint FK8C04C7B64689953 
        foreign key (CODPOSTO) 
        references SGF.tb_posto;

    alter table SGF.TB_ABASTECIMENTO 
        add constraint FK8C04C7B6F1DDBFEB 
        foreign key (CODTIPOSERVICO) 
        references SGF.tb_tiposervico;

    alter table SGF.TB_ABASTECIMENTO 
        add constraint FK8C04C7B6EC4189FF 
        foreign key (CODTIPOCOMBUSTIVEL) 
        references SGF.tb_tipocombustivel;

    alter table SGF.TB_ABASTECIMENTO 
        add constraint FK8C04C7B6E0DE9D4D 
        foreign key (CODMOTORISTA) 
        references SGF.tb_pessoamotorista;

    alter table SGF.TB_ABASTECIMENTO 
        add constraint FK8C04C7B6E0725648 
        foreign key (CODUSUARIOAUT) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_ATENDABASTECIMENTO 
        add constraint FK4548DA902DDD5B3 
        foreign key (CODBOMBA) 
        references SGF.tb_bomba;

    alter table SGF.TB_ATENDABASTECIMENTO 
        add constraint FK4548DA90E48F14B4 
        foreign key (CODUSUARIO) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_ATENDABASTECIMENTO 
        add constraint FK4548DA90E7E30E3 
        foreign key (CODSOLABASTECIMENTO) 
        references SGF.TB_ABASTECIMENTO;

    alter table SGF.TB_CADPESSOA 
        add constraint FK5835863C4629AC12 
        foreign key (COD_UA) 
        references SGF.TB_UA;

    alter table SGF.TB_CADVEICULO 
        add constraint FKEB4C8BFA7E5F69B7 
        foreign key (CODMODELO) 
        references SGF.TB_MODELO;

    alter table SGF.TB_CADVEICULO 
        add constraint FKEB4C8BFAE22CDED 
        foreign key (CODESPECIE) 
        references SGF.TB_ESPECIE;

    alter table SGF.TB_CADVEICULO 
        add constraint FKEB4C8BFA5B91C44A 
        foreign key (COD_UA_ASI) 
        references SGF.TB_UA;

    alter table SGF.TB_GRUPOUSUARIO 
        add constraint FKB074F8D6A2D6A52D 
        foreign key (CODPESSOAUSUARIO) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_GRUPOUSUARIO 
        add constraint FKB074F8D662DE049A 
        foreign key (codgrupo) 
        references SGF.tb_grupo;

    alter table SGF.TB_LOGUSUARIO 
        add constraint FKB8E8E1DB37F11608 
        foreign key (ID_USUARIO) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_MODELO 
        add constraint FK97D05957407462D 
        foreign key (CODMARCA) 
        references SGF.TB_MARCA;

    alter table SGF.TB_MULTA 
        add constraint FK5FBF4000E4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.TB_MULTA 
        add constraint FK5FBF4000E48F14B4 
        foreign key (CODUSUARIO) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_MULTA 
        add constraint FK5FBF40002B83F386 
        foreign key (CODPESSOAMOTORISTA) 
        references SGF.tb_pessoamotorista;

    alter table SGF.TB_MULTA 
        add constraint FK5FBF4000C38D7E2D 
        foreign key (CODINFRACAO) 
        references SGF.TB_INFRACAO;

    alter table SGF.TB_NUMEROMOTOR 
        add constraint FK4C1C968E4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.TB_NUMEROMOTOR 
        add constraint FK4C1C968E48F14B4 
        foreign key (CODUSUARIO) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_PESSOAUSUARIO 
        add constraint FK59B065768790B439 
        foreign key (CODPESSOA) 
        references SGF.TB_CADPESSOA;

    alter table SGF.TB_PESSOAUSUARIO 
        add constraint FK59B065764689953 
        foreign key (CODPOSTO) 
        references SGF.tb_posto;

    alter table SGF.TB_REGISTROVEICULOS 
        add constraint FK3BAEC128A2D6A52D 
        foreign key (codpessoausuario) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_REGISTROVEICULOS 
        add constraint FK3BAEC128D110F294 
        foreign key (CODSOLVEICULO) 
        references SGF.TB_SOLVEICULOS;

    alter table SGF.TB_REGISTROVEICULOS 
        add constraint FK3BAEC1282B83F386 
        foreign key (codpessoamotorista) 
        references SGF.tb_pessoamotorista;

    alter table SGF.TB_REQUISICAOITEM 
        add constraint FK1F43BDCDF1DDBFEB 
        foreign key (CODTIPOSERVICO) 
        references SGF.tb_tiposervico;

    alter table SGF.TB_REQUISICAOITEM 
        add constraint FK1F43BDCD42B00460 
        foreign key (CODREQMANUTENCAO) 
        references SGF.TB_REQUISICAOMANUTENCAO;

    alter table SGF.TB_REQUISICAOMANUTENCAO 
        add constraint FK689547E9E4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.TB_REQUISICAOMANUTENCAO 
        add constraint FK689547E9E48F14B4 
        foreign key (CODUSUARIO) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_REQUISICAOMANUTENCAO 
        add constraint FK689547E92FA828B 
        foreign key (CODOFICINA) 
        references SGF.TB_OFICINA;

    alter table SGF.TB_SOLTROCALUBRIFICANTE 
        add constraint FK876D3E5EE4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.TB_SOLTROCALUBRIFICANTE 
        add constraint FK876D3E5E4689953 
        foreign key (CODPOSTO) 
        references SGF.tb_posto;

    alter table SGF.TB_SOLTROCALUBRIFICANTE 
        add constraint FK876D3E5E2B83F386 
        foreign key (CODPESSOAMOTORISTA) 
        references SGF.tb_pessoamotorista;

    alter table SGF.TB_SOLTROCALUBRIFICANTE 
        add constraint FK876D3E5EF1DDBFEB 
        foreign key (CODTIPOSERVICO) 
        references SGF.tb_tiposervico;

    alter table SGF.TB_SOLTROCALUBRIFICANTE 
        add constraint FK876D3E5EE0725648 
        foreign key (CODUSUARIOAUT) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_SOLVEICULOS 
        add constraint FKCAB7C763E4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.TB_SOLVEICULOS 
        add constraint FKCAB7C763E0729918 
        foreign key (CODUSUARIOSOL) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_SOLVEICULOS 
        add constraint FKCAB7C763E0725648 
        foreign key (CODUSUARIOAUT) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.TB_SOLVEICULOS 
        add constraint FKCAB7C7632CC3F8C5 
        foreign key (CODVEICULOAUT) 
        references SGF.TB_CADVEICULO;

    alter table SGF.TB_TRANSMISSAO 
        add constraint FK8A3CFE6146873C9 
        foreign key (CODPONTO) 
        references SGF.TB_PONTO;

    alter table SGF.TB_UA 
        add constraint FK4BF25FD4629AC1E 
        foreign key (COD_UG) 
        references SGF.tb_ug;

    alter table SGF.TB_ULTIMATRANSMISSAO 
        add constraint FK636321419CCD7E34 
        foreign key (CODPONTOPROX) 
        references SGF.TB_PONTO;

    alter table SGF.TB_ULTIMATRANSMISSAO 
        add constraint FK63632141E4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.TB_VEICULO_AREA 
        add constraint FK263E250EE4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.TB_VEICULO_AREA 
        add constraint FK263E250ECE8A74C5 
        foreign key (CODAREA) 
        references SGF.TB_AREA;

    alter table SGF.tb_bomba 
        add constraint FKF9E080EE4689953 
        foreign key (CODPOSTO) 
        references SGF.tb_posto;

    alter table SGF.tb_bomba 
        add constraint FKF9E080EEEC4189FF 
        foreign key (CODTIPOCOMBUSTIVEL) 
        references SGF.tb_tipocombustivel;

    alter table SGF.tb_cotaabastecimento 
        add constraint FKC1A1BD1DE4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.tb_cotaabastecimento 
        add constraint FKC1A1BD1DF1DDBFEB 
        foreign key (CODTIPOSERVICO) 
        references SGF.tb_tiposervico;

    alter table SGF.tb_diariobomba 
        add constraint FKC2AED0122DDD5B3 
        foreign key (codbomba) 
        references SGF.tb_bomba;

    alter table SGF.tb_diariobomba 
        add constraint FKC2AED012E48F14B4 
        foreign key (codusuario) 
        references SGF.TB_PESSOAUSUARIO;

    alter table SGF.tb_grupopage 
        add constraint FKFFC369E7CE9798C9 
        foreign key (codpage) 
        references SGF.TB_PAGE;

    alter table SGF.tb_grupopage 
        add constraint FKFFC369E762DE049A 
        foreign key (codgrupo) 
        references SGF.tb_grupo;

    alter table SGF.tb_grupopermissao 
        add constraint FK1233592F6180D243 
        foreign key (codpermissao) 
        references SGF.tb_permissao;

    alter table SGF.tb_grupopermissao 
        add constraint FK1233592F62DE049A 
        foreign key (codgrupo) 
        references SGF.tb_grupo;

    alter table SGF.tb_pessoamotorista 
        add constraint FKB562D3748790B439 
        foreign key (CODPESSOA) 
        references SGF.TB_CADPESSOA;

    alter table SGF.tb_postoservico 
        add constraint FKABAF2414689953 
        foreign key (CODPOSTO) 
        references SGF.tb_posto;

    alter table SGF.tb_postoservico 
        add constraint FKABAF241F1DDBFEB 
        foreign key (CODTIPOSERVICO) 
        references SGF.tb_tiposervico;

    alter table SGF.tb_postoservico 
        add constraint FKABAF241EC4189FF 
        foreign key (CODTIPOCOMBUSTIVEL) 
        references SGF.tb_tipocombustivel;

    alter table SGF.tb_postoservicoveiculo 
        add constraint FKA10C62AEE4286893 
        foreign key (CODVEICULO) 
        references SGF.TB_CADVEICULO;

    alter table SGF.tb_postoservicoveiculo 
        add constraint FKA10C62AE482AF8AB 
        foreign key (CODPOSTOSERVICO) 
        references SGF.tb_postoservico;

    alter table SGF.TB_ATENDTROCALUB 
        add constraint FK99BC77614EDF3482 
        foreign key (CODSOLTROCALUB) 
        references SGF.TB_SOLTROCALUBRIFICANTE;

    alter table SGF.TB_ATENDTROCALUB 
        add constraint FK99BC776162BBCCD0 
        foreign key (CODUSUARIOATEND) 
        references SGF.TB_PESSOAUSUARIO;

    create sequence codnumeromotor_seq;

    create sequence codsoltrocalub_seq;

    create table sgf.hibernate_sequences (
         sequence_name varchar(255),
         sequence_next_hi_value int4 
    );

    create sequence sgf.abastecimento_seq;

    create sequence sgf.atendabastecimento_seq;

    create sequence sgf.codbomba_seq;

    create sequence sgf.codcotaabastecimento_seq;

    create sequence sgf.coddiariobomba_seq;

    create sequence sgf.codespecie_seq;

    create sequence sgf.codgrupo_seq;

    create sequence sgf.codinfracao_seq;

    create sequence sgf.codmarca_seq;

    create sequence sgf.codmodelo_seq;

    create sequence sgf.codmulta_seq;

    create sequence sgf.codoficina_seq;

    create sequence sgf.codpermissao_seq;

    create sequence sgf.codpessoa_seq;

    create sequence sgf.codpessoamotorista_seq;

    create sequence sgf.codpessoausuario_seq;

    create sequence sgf.codposto_seq;

    create sequence sgf.codpostoservico_seq;

    create sequence sgf.codpostoservicoveiculo_seq;

    create sequence sgf.codsolveiculo_seq;

    create sequence sgf.codtipocombustivel_seq;

    create sequence sgf.codtiposervico_seq;

    create sequence sgf.codua_seq;

    create sequence sgf.codveiculo_seq;
