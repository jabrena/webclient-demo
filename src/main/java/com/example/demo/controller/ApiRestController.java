package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.dto.AnuaReportsDTO;
import com.example.demo.dto.IntradayAndBalanceDTO;
import com.example.demo.dto.IntradayDTO;

import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping("/api")
@Log
public class ApiRestController {

	@Autowired
	WebClient webClient;

	private String url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&interval=1min&outputsize=full&apikey=AN2UGRW0CBO3PWLD";

	private String reports_url = "https://www.alphavantage.co/query?function=BALANCE_SHEET&apikey=AN2UGRW0CBO3PWLD";

	@GetMapping(path = "/history-blocking")
	public List<IntradayDTO> historyIntraday(@RequestParam("symbol") List<String> symbols) {
		List<IntradayDTO> response = new ArrayList<IntradayDTO>();

		for (String symbol : symbols) {
			log.info("START Retreive Blocking for " + symbol);
			RestTemplate restTemplate = new RestTemplate();
			ResponseEntity<IntradayDTO> partialResult = restTemplate.exchange(url + "&symbol=" + symbol, HttpMethod.GET,
					null, IntradayDTO.class);
			log.info("END Retreive Blocking for " + symbol);
			response.add(partialResult.getBody());
		}

		return response;

	}

	@GetMapping(value = "/history-non-blocking-flux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<IntradayDTO> historyIntradayNonBlocking(@RequestParam("symbol") List<String> symbols) {
		log.info("START Retreive Flux for " + symbols);
		Flux<IntradayDTO> response = Flux.fromIterable(symbols)
				.parallel()
				.runOn(Schedulers.boundedElastic())
				.flatMap(this::historyIntradayNonBlockingMono)
				.ordered((u1, u2) -> -1);
		log.info("START Retreive Flux for " + symbols);
		return response;

	}

	@GetMapping(value = "/history-non-blocking-mono")
	private Mono<IntradayDTO> historyIntradayNonBlockingMono(String symbol) {

		log.info("START Retreive Intradia for " + symbol);

//		Mono<IntradayDTO> response = webClient.get()
//				.uri(url + "&symbol=" + symbol)
//				.accept(MediaType.APPLICATION_JSON)
//				.exchangeToMono(r -> {
//					return r.bodyToMono(IntradayDTO.class);
//				});

		Mono<IntradayDTO> response = webClient.get()
				.uri(url + "&symbol=" + symbol)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(IntradayDTO.class);
		log.info("END Retreive Intradia for " + symbol);
		return response;
	}

	@GetMapping(value = "/anual-reports-non-blocking-mono")
	private Mono<AnuaReportsDTO> anualReports(String symbol) {
		log.info("START Retreive Report for " + symbol);
		Mono<AnuaReportsDTO> response = webClient.get()
				.uri(reports_url + "&symbol=" + symbol)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.bodyToMono(AnuaReportsDTO.class);
		log.info("END Retreive Report for " + symbol);
		return response;

	}

	@GetMapping(value = "/anual-reports-with-balance")
	private Mono<IntradayAndBalanceDTO> intraDiaAndBalance(String symbol) {

		Mono<IntradayDTO> intradia = this.historyIntradayNonBlockingMono(symbol)
				.subscribeOn(Schedulers.boundedElastic());
		Mono<AnuaReportsDTO> anualReports = this.anualReports(symbol)
				.subscribeOn(Schedulers.boundedElastic());

		return Mono.zip(intradia, anualReports, IntradayAndBalanceDTO::new);

	}

}
