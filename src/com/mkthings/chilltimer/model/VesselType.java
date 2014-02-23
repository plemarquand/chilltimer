package com.mkthings.chilltimer.model;

public enum VesselType {
	FRIDGE(5, -0.03644631036872431, 0.05), FREEZER(0, -0.07335731489685161, 0.01), ICE(4, -0.06347363947913902, 0.005), ICE_WATER(0, -0.16695373135037633, 0), SALTED_ICE_WATER(-4, -0.23221448811157558, 0), OUTSIDE(
			0, -0.07335731489685161, 0);

	private double ambientTemperature;

	// Data for calculating k for various k values taken from MythBusters
	// http://www.youtube.com/watch?v=M_mFKwTCQCQ&t=7m25s
	private double k;
	private double quantityScaleFactor;

	VesselType(double ambientTemperature, double k, double quantityScaleFactor) {
		this.ambientTemperature = ambientTemperature;
		this.k = k;
		this.quantityScaleFactor = quantityScaleFactor;
	}

	public double getAmbientTemperature() {
		return ambientTemperature;
	}

	public double getK() {
		return k;
	}

	public double getQuantityScaleFactor() {
		return quantityScaleFactor;
	}

	public void setAmbientTemperature(int temperature) {
		this.ambientTemperature = temperature;
	}
}