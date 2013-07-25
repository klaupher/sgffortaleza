package br.gov.ce.fortaleza.cti.sgf.util;


public enum StatusVeiculo {
//	//TODO VERIFICAR COM O ANDRÉ O QUE É VEÍCULO LOCADO???
//	public static Integer 
//			DISPONIVEL 	= 0, 
//			LOCADO     	= 1, 
//			RESERVADO  	= 2, 
//			EM_MANUTENCAO = 3, 
//			EM_SERVICO 	= 4, 
//			BAIXADO 	= -1, 
//			TRANSF_EXTERNA = 5;
	
	disponivel(0,"disponivel"),
	locado(1,"locado"),
	reservado(2,"reservado"),
	emManutencao(3,"emManutencao"),
	emServico(4,"emServico"),
	transfExterna(5,"transfExterna"),
	baixado(6,"baixado");
	
	public Integer valor;
	public String label;
	
	private StatusVeiculo(Integer valor, String label) {
		this.label = label;
		this.valor = valor;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getLabel();
	}

	public Integer getValor() {
		return valor;
	}

	public void setValor(Integer valor) {
		this.valor = valor;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
}
