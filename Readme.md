# Multithread_Webcrawler

Multithread Webcrawler is a webcrawler written in Java8 and uses Jsoup library. It was made as a freelancing project. The main idea is to score websites higher if they reference the same domain links more.

## Formula for scoring
```
sameBaseUrl/(sameBaseUrl + diffBaseUrl)
```

## Installation

- Pull code through this [link](https://github.com/snowsanta/Multithread_Webcrawler.git)
- Run in Java8 environment

## Usage

`compiled name` <Base_link> <max_Depth>

The crawler starts from <Base_link> and goes upto <max_Depth> depth.


## License

[MIT](https://choosealicense.com/licenses/mit/)