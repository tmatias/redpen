<!-- @suppress SpaceBetweenAlphabeticalWord -->
# 分散処理
最近利用されているソフトウェアの中には複数の計算機上で動作（分散）するものが多く存在し、このような分散ソフトウェアは複数の計算機で動作することで一台では処理が追いつかない大量のデータを扱えたり、
高負荷な状況に対処できたり、可用性を向上できたりします。本稿では,複数の計算機（Cluster)でで動作する各サーバーを**インスタンス**と呼びます。
たとえば検索エンジンやデータベースではインデックスを複数のインスタンスで分割して保持します。
このような場合、クラスターの各インデクスが返す結果をマージしてクライアントにわたす機構が必要です。
