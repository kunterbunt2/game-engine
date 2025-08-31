# Models

| Model Name | Size  | Languages | Datasets                  | Architectures / Models | Notes                            |
|------------|-------|-----------|---------------------------|------------------------|----------------------------------|
| xtts_v2    | 2gb   | multi     | vctk,libritts,commonvoice | vits,hifigan           | High quality, multi-speaker TTS  |
| your_tts   | 350mb | multi     | vctk,libritts,commonvoice | vits,hifigan           | Good quality, multi-speaker TTS  |
| bark       | 4gb   | multi     | vctk,libritts,commonvoice | vits,hifigan           | State-of-the-art, expressive TTS |

# Languages

| Abbrev. | Meaning      |
|---------|--------------|
| en      | English      |
| fr      | French       |
| de      | German       |
| es      | Spanish      |
| it      | Italian      |
| pt      | Portuguese   |
| ru      | Russian      |
| zh      | Chinese      |
| ja      | Japanese     |
| ko      | Korean       |
| multi   | Multilingual |

# Datasets

| Abbrev.     | Dataset Name         | Notes                                   |
|-------------|----------------------|-----------------------------------------|
| vctk        | VCTK Corpus          | 109 speakers, English accents           |
| ljspeech    | LJ Speech            | Single female speaker, 24h audiobooks   |
| libritts    | LibriTTS             | Multi-speaker, audiobook-derived, ~585h |
| commonvoice | Mozilla Common Voice | Crowdsourced, many languages            |
| blizzard    | Blizzard Challenge   | Audiobook narrations                    |
| aishell     | AISHELL-3            | Chinese multi-speaker dataset           |
| css10       | CSS10                | 10-language single-speaker datasets     |
| emov-db     | EmoV-DB              | Emotional speech dataset                |

# Architectures / Models

| Abbrev.         | Full Name                 | Notes                               | Strengths / Typical Use Case               |
|-----------------|---------------------------|-------------------------------------|--------------------------------------------|
| vits            | Variational Inference TTS | End-to-end, natural speech (2021)   | High quality, natural prosody, fewer steps |
| tacotron / 2    | Tacotron (2)              | Text-to-mel, needs separate vocoder | Classic baseline, good quality, slower     |
| fastspeech / 2  | FastSpeech (2)            | Parallel, fast training & inference | Very fast, scalable, decent quality        |
| glowtts         | Glow-TTS                  | Flow-based, expressive speech       | More control over speaking style           |
| hifigan         | HiFi-GAN                  | Neural vocoder (mel → waveform)     | High-fidelity audio, very common vocoder   |
| waveglow        | WaveGlow                  | Flow-based vocoder                  | Decent quality, less used now              |
| wavernn         | WaveRNN                   | Compact neural vocoder              | Lightweight, runs on lower-end devices     |
| parallelwavegan | Parallel WaveGAN          | GAN-based vocoder, efficient        | Fast, resource-efficient                   |
| styletts        | StyleTTS                  | Adds style & prosody modeling       | Good for expressive, emotional TTS         |
| conformer       | Conformer-based TTS       | Transformer + CNN hybrid            | Handles long text well, stable output      |

# Docker image

Coqui running in docker container stores the pulled models in = /root/.local/share/tts

## tts_models/en/vctk/vits

| Ref | Speaker ID | Gender | Accent / Region               |
|-----|------------|--------|-------------------------------|
| 1   | p225       | F      | English: Southern England     |
| 2   | p226       | M      | English: Surrey               |
| 3   | p227       | M      | English: Cumbria              |
| 4   | p228       | F      | English: Southern England     |
| 5   | p229       | F      | English: Southern England     |
| 6   | p230       | F      | English: Stockton-on-Tees     |
| 7   | p231       | F      | English: Southern England     |
| 8   | p232       | M      | English: Southern England     |
| 9   | p233       | F      | English: Staffordshire        |
| 10  | p234       | F      | Scottish: West Dumfries       |
| 11  | p236       | F      | English: Manchester           |
| 12  | p237       | M      | Scottish: Fife                |
| 13  | p238       | F      | Northern Irish: Belfast       |
| 14  | p239       | F      | English: SW England           |
| 15  | p240       | F      | English: Southern England     |
| 16  | p241       | M      | Scottish: Perth               |
| 17  | p243       | M      | English: London               |
| 18  | p244       | F      | English: Manchester           |
| 19  | p245       | M      | Irish: Dublin                 |
| 20  | p246       | M      | Scottish: Selkirk             |
| 21  | p247       | M      | Scottish: Argyll              |
| 22  | p248       | F      | Indian                        |
| 23  | p249       | F      | Scottish: Aberdeen            |
| 24  | p250       | F      | English: SE England           |
| 25  | p251       | M      | Indian                        |
| 26  | p252       | M      | Scottish: Edinburgh           |
| 27  | p253       | F      | Welsh: Cardiff                |
| 28  | p254       | M      | English: Surrey               |
| 29  | p255       | M      | Scottish: Galloway            |
| 30  | p256       | M      | English: Birmingham           |
| 31  | p257       | F      | English: Southern England     |
| 32  | p258       | M      | English: Southern England     |
| 33  | p259       | M      | English: Nottingham           |
| 34  | p260       | M      | Scottish: Orkney              |
| 35  | p261       | F      | Northern Irish: Belfast       |
| 36  | p262       | F      | Scottish: Edinburgh           |
| 37  | p263       | M      | Scottish: Aberdeen            |
| 38  | p264       | F      | Scottish: West Lothian        |
| 39  | p265       | F      | Scottish: Ross                |
| 40  | p266       | F      | Irish: Athlone                |
| 41  | p267       | F      | English: Yorkshire            |
| 42  | p268       | F      | English: Southern England     |
| 43  | p269       | F      | English: Newcastle            |
| 44  | p270       | M      | English: Yorkshire            |
| 45  | p271       | M      | Scottish: Fife                |
| 46  | p272       | M      | Scottish: Edinburgh           |
| 47  | p273       | M      | English: Suffolk              |
| 48  | p274       | M      | English: Essex                |
| 49  | p275       | M      | Scottish: Midlothian          |
| 50  | p276       | F      | English: Oxford               |
| 51  | p277       | F      | English: NE England           |
| 52  | p278       | M      | English: Cheshire             |
| 53  | p279       | M      | English: Leicester            |
| 54  | p280       | –      | Unknown                       |
| 55  | p281       | M      | Scottish: Edinburgh           |
| 56  | p282       | F      | English: Newcastle            |
| 57  | p283       | F      | Irish: Cork                   |
| 58  | p284       | M      | Scottish: Fife                |
| 59  | p285       | M      | Scottish: Edinburgh           |
| 60  | p286       | M      | English: Newcastle            |
| 61  | p287       | M      | English: York                 |
| 62  | p288       | F      | Irish: Dublin                 |
| 63  | p292       | M      | Northern Irish: Belfast       |
| 64  | p293       | F      | Northern Irish: Belfast       |
| 65  | p294       | F      | American: San Francisco       |
| 66  | p295       | F      | Irish: Dublin                 |
| 67  | p297       | F      | American: New York            |
| 68  | p298       | M      | Irish: Tipperary              |
| 69  | p299       | F      | American: California          |
| 70  | p300       | F      | American: California          |
| 71  | p301       | F      | American: North Carolina      |
| 72  | p302       | M      | Canadian: Montreal            |
| 73  | p303       | F      | Canadian: Toronto             |
| 74  | p304       | M      | Northern Irish: Belfast       |
| 75  | p305       | F      | American: Philadelphia        |
| 76  | p306       | F      | American: New York            |
| 77  | p307       | F      | Canadian: Ontario             |
| 78  | p308       | F      | American: Alabama             |
| 79  | p310       | F      | American: Tennessee           |
| 80  | p311       | M      | American: Iowa                |
| 81  | p312       | F      | Canadian: Hamilton            |
| 82  | p313       | F      | Irish: County Down            |
| 83  | p314       | F      | South African: Cape Town      |
| 84  | p316       | M      | Canadian: Alberta             |
| 85  | p317       | F      | Canadian: Hamilton            |
| 86  | p318       | F      | American: Napa                |
| 87  | p323       | F      | South African: Pretoria       |
| 88  | p326       | M      | Australian: Sydney            |
| 89  | p329       | F      | American (unspecified)        |
| 90  | p330       | F      | American (unspecified)        |
| 91  | p333       | F      | American: Indiana             |
| 92  | p334       | M      | American: Chicago             |
| 93  | p335       | F      | New Zealand English           |
| 94  | p336       | F      | South African: Johannesburg   |
| 95  | p339       | F      | American: Pennsylvania        |
| 96  | p340       | F      | Irish: Dublin                 |
| 97  | p341       | F      | American: Ohio                |
| 98  | p343       | F      | Canadian: Alberta             |
| 99  | p345       | M      | American: Florida             |
| 100 | p347       | M      | South African: Johannesburg   |
| 101 | p351       | F      | Northern Irish: Derry         |
| 102 | p360       | M      | American: New Jersey          |
| 103 | p361       | F      | American: New Jersey          |
| 104 | p362       | F      | American (unspecified)        |
| 105 | p363       | M      | Canadian: Toronto             |
| 106 | p364       | M      | Irish: Donegal                |
| 107 | p374       | M      | Australian: English (general) |
