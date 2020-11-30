package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data

public class IntradayDTO {

	@JsonProperty("Meta Data")
	private MetaDataDTO metaData;

//	@JsonProperty("Time Series (1min)")
//	private List<TimeSerieDTO> timeSeries;

}
