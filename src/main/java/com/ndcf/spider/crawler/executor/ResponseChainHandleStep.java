package com.ndcf.spider.crawler.executor;

import com.ndcf.spider.crawler.dto.DTOHttpConnect;

public interface ResponseChainHandleStep {
     DTOHttpConnect handle(DTOHttpConnect dtoHttpConnect);
}
