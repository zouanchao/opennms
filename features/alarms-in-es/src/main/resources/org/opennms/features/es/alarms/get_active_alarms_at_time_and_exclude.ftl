{
  <#-- Only retrieve the 'id' and 'reduction-key' fields -->
  "_source": ["id", "reduction-key"],
  "query": {
    "bool" : {
      "filter": [
        {
          "range": {
            "@update-time": {
              "lte": ${time?long?c},
              "format": "epoch_millis"
            }
          }
        }
      ]
    }
  },
  "aggs": {
    "alarms_by_id": {
      "terms": {
        "field": "id",
        "size": ${numMaxAlarms?long?c}
      },
      "aggs": {
        "latest_alarm": {
          "top_hits": {
            "sort": [
              {
                "@update-time": {
                  "order": "desc"
                }
              }
            ],
            "size" : 1
          }
        }
      }
    }
  },
  "post_filter": {
    "bool" : {
      "must_not": [
        {
          "range": {
            "@deleted-time": {
              "lte": ${time?long?c},
              "format": "epoch_millis"
            }
          }
        }
      ]
    }
  }
}