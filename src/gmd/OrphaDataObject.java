package gmd;

public class OrphaDataObject {

	private int orphaNumber;
	private String disease;
	private String clinicalSign;
	private String frequency;
	
	public OrphaDataObject(int orphaNumber, String disease, String clinicalSign, String frequency) {
		this.orphaNumber = orphaNumber;
		this.disease = disease;
		this.clinicalSign = clinicalSign;
		this.frequency = frequency;
	}

	public int getOrphaNumber() {
		return orphaNumber;
	}

	public void setOrphaNumber(int orphaNumber) {
		this.orphaNumber = orphaNumber;
	}

	public String getDisease() {
		return disease;
	}

	public void setDisease(String disease) {
		this.disease = disease;
	}

	public String getClinicalSign() {
		return clinicalSign;
	}

	public void setClinicalSign(String clinicalSign) {
		this.clinicalSign = clinicalSign;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
}