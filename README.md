
注意：LunaChatが導入されているサーバーでは、以下の設定が必要です。

```
# -------------------- 通常チャット設定 --------------------

# 通常チャット（非チャンネルチャット）の装飾を、LunaChatから行うかどうか。
enableNormalChatMessageFormat: true

# チャット装飾のフォーマット設定。
# フォーマット設定には、下記のキーワードが使用できます。
# %displayname : 発言者表示名
# %player   : 発言者ID
# %world    : 発言したワールド名（spigot側に導入したときに有効です。MultiVerseが導入されている場合は、ワールドの表示名を取得して使用します。）
# %server   : 発言者の接続サーバー名（BungeeCord側に導入したときに有効です。）
# %prefix   : プレフィックス（VaultとPrefix/Suffixプラグインが導入されている場合に置き換えられます）
# %suffix   : サフィックス（VaultとPrefix/Suffixプラグインが導入されている場合に置き換えられます）
# %date     : 日付
# %time     : 時刻
# %msg      : 発言内容（Japanize変換された場合は、Japanize結果を含みます。）
normalChatMessageFormat: '&f%prefix%displayname%suffix&a:&f %msg'
#normalChatMessageFormat: '&f%displayname@%server&a:&f %msg'

# 通常チャット（非チャンネルチャット）で、カラーコード（&aや&bなど）を
# 使用可能にするかどうか。falseに設定すると、カラーコードは変換されません。
enableNormalChatColorCode: false <- ここをfalseにしてください。デフォルトではtrueです。

# 通常チャット（非チャンネルチャット）をクリック可能にするかどうか。
# クリック可能にすると、プレイヤー名をクリックしてプライベートメッセージを送ることができるようになりますが、
# 通常チャットをキャンセルしてクリック可能な別のメッセージを流すので、他のチャット関連プラグインとの連携が
# うまくいかなくなる可能性があります。
# なお、この設定に関係なく、チャンネルチャットは常にクリック可能です。
enableNormalChatClickable: false

# 通常チャット（非チャンネルチャット）をコンソールにログ出力するかどうか。
# 本項目をfalseにしても、コンソールに記録される場合があります。
displayNormalChatOnConsole: true
```

コマンド：
/menu メニューを開きます <br>
/prefixtoggle prefix結合の有効無効を切り替えます <br>
/load　データベースからニックネームを読み込みます <br>
