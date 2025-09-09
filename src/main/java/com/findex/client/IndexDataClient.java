package com.findex.client;

import com.findex.entity.IndexInfo;

public interface IndexDataClient {
  IndexDataSnapshot fetch(IndexInfo target);
}
