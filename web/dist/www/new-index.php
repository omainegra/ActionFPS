<?php
$wide = true;
require_once("render.inc.php");
require("render_game.inc.php");
?>

<article id="ladder" class="bordered">
    <div class="profile">
        <h2><a href="/ladder/">Ladder</a></h2>
        <table>
            <thead>
            <tr>
                <th>User</th>
                <th>Points</th>
                <th>Flags</th>
                <th>Frags</th>
                <th>Gibs</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <th><a href="/player/?id=madcatz"> madcatz </a></th>
                <td> 758</td>
                <td> 9</td>
                <td> 274</td>
                <td> 25</td>
            </tr>
            <tr>
                <th><a href="/player/?id=yang"> yang </a></th>
                <td> 600</td>
                <td> 10</td>
                <td> 216</td>
                <td> 6</td>
            </tr>
            <tr>
                <th><a href="/player/?id=ketar"> ketar </a></th>
                <td> 430</td>
                <td> 12</td>
                <td> 122</td>
                <td> 2</td>
            </tr>
            <tr>
                <th><a href="/player/?id=thirtytwo"> thirtytwo </a></th>
                <td> 328</td>
                <td> 6</td>
                <td> 71</td>
                <td> 32</td>
            </tr>
            <tr>
                <th><a href="/player/?id=drakas"> drakas </a></th>
                <td> 302</td>
                <td> 0</td>
                <td> 49</td>
                <td> 68</td>
            </tr>
            <tr>
                <th><a href="/player/?id=crash"> crash </a></th>
                <td> 238</td>
                <td> 6</td>
                <td> 68</td>
                <td> 4</td>
            </tr>
            <tr>
                <th><a href="/player/?id=suicidesquad"> suicidesquad </a></th>
                <td> 207</td>
                <td> 7</td>
                <td> 48</td>
                <td> 2</td>
            </tr>
            <tr>
                <th><a href="/player/?id=brett"> brett </a></th>
                <td> 207</td>
                <td> 7</td>
                <td> 45</td>
                <td> 4</td>
            </tr>
            <tr>
                <th><a href="/player/?id=vfl"> vfl </a></th>
                <td> 184</td>
                <td> 5</td>
                <td> 50</td>
                <td> 3</td>
            </tr>
            </tbody>
        </table>
    </div>
</article>
<div class="bordered" id="bulletins">
    <div class="bulletin">
        <div class="when">
            <time is="relative-time" datetime="2016-05-09T00:00:00Z" title="May 9, 2016, 8:00 AM GMT+8">5 days ago
            </time>
        </div>
        <div class="cnt">
            Added the
            <a href="/ladder/">Ladder</a> page. We'll be incrementally updating it, this is just an
            <a href="https://en.wikipedia.org/wiki/Minimum_viable_product" target="_blank">MVP</a>. The server is
            <strong>aura.woop.ac</strong> and
            <strong>califa.actionfps.com</strong>, default port, and is running as a public server!
        </div>
    </div>
</div>
<article id="player-ranks" class="bordered">
    <div id="rank">
        <h2>Player Ranks</h2>
        <table style="width: 480px ;">
            <tbody>
            <tr>
                <td>Rank</td>
                <td>Player</td>
                <td>Games</td>
                <td>Won</td>
                <td>Elo</td>
                <td>Score</td>
                <td>Last played</td>
            </tr>
            <tr>
                <td>1</td>
                <td><a href="/player/?id=graziano">|DnC|Grazy</a></td>
                <td>149</td>
                <td>102</td>
                <td>1909</td>
                <td>114523</td>
                <td><a href="/game/?id=2016-05-12T20:58:31Z"><time is="relative-time" datetime="2016-05-12T20:58:31Z">2016-05-12T20:58:31Z</time></a> </td>
            </tr>
            <tr>
                <td>2</td>
                <td><a href="/player/?id=honor">w00p|Honor</a></td>
                <td>217</td>
                <td>141</td>
                <td>1881</td>
                <td>184401</td>
                <td><a href="/game/?id=2016-05-07T10:47:54Z"><time is="relative-time" datetime="2016-05-07T10:47:54Z">2016-05-07T10:47:54Z</time></a> </td>
            </tr>
            <tr>
                <td>3</td>
                <td><a href="/player/?id=lucas">w00p|Lucas</a></td>
                <td>763</td>
                <td>396</td>
                <td>1714</td>
                <td>497037</td>
                <td><a href="/game/?id=2016-02-13T22:56:00Z"><time is="relative-time" datetime="2016-02-13T22:56:00Z">2016-02-13T22:56:00Z</time></a> </td>
            </tr>
            <tr>
                <td>4</td>
                <td><a href="/player/?id=mise">Mise=MyS=</a></td>
                <td>335</td>
                <td>212</td>
                <td>1714</td>
                <td>227866</td>
                <td><a href="/game/?id=2016-05-12T13:46:19Z"><time is="relative-time" datetime="2016-05-12T13:46:19Z">2016-05-12T13:46:19Z</time></a> </td>
            </tr>
            <tr>
                <td>5</td>
                <td><a href="/player/?id=chill">.45|Chill</a></td>
                <td>107</td>
                <td>79</td>
                <td>1709</td>
                <td>86770</td>
                <td><a href="/game/?id=2016-05-12T22:52:10Z"><time is="relative-time" datetime="2016-05-12T22:52:10Z">2016-05-12T22:52:10Z</time></a> </td>
            </tr>
            <tr>
                <td>6</td>
                <td><a href="/player/?id=fury">iR|FURY</a></td>
                <td>367</td>
                <td>229</td>
                <td>1705</td>
                <td>313974</td>
                <td><a href="/game/?id=2016-05-12T23:34:41Z"><time is="relative-time" datetime="2016-05-12T23:34:41Z">2016-05-12T23:34:41Z</time></a> </td>
            </tr>
            <tr>
                <td>7</td>
                <td><a href="/player/?id=sanzo">w00p|Sanzo</a></td>
                <td>390</td>
                <td>224</td>
                <td>1691</td>
                <td>282461</td>
                <td><a href="/game/?id=2016-05-09T16:40:41Z"><time is="relative-time" datetime="2016-05-09T16:40:41Z">2016-05-09T16:40:41Z</time></a> </td>
            </tr>
            <tr>
                <td>8</td>
                <td><a href="/player/?id=ven">queensKing</a></td>
                <td>145</td>
                <td>89</td>
                <td>1671</td>
                <td>111158</td>
                <td><a href="/game/?id=2016-03-06T16:22:06Z"><time is="relative-time" datetime="2016-03-06T16:22:06Z">2016-03-06T16:22:06Z</time></a> </td>
            </tr>
            <tr>
                <td>9</td>
                <td><a href="/player/?id=enty">iR|.EnTy^</a></td>
                <td>310</td>
                <td>214</td>
                <td>1565</td>
                <td>201781</td>
                <td><a href="/game/?id=2016-05-12T23:52:39Z"><time is="relative-time" datetime="2016-05-12T23:52:39Z">2016-05-12T23:52:39Z</time></a> </td>
            </tr>
            <tr>
                <td>10</td>
                <td><a href="/player/?id=lean">|DnC|LeaN~</a></td>
                <td>79</td>
                <td>49</td>
                <td>1553</td>
                <td>55266</td>
                <td><a href="/game/?id=2016-05-03T17:54:01Z"><time is="relative-time" datetime="2016-05-03T17:54:01Z">2016-05-03T17:54:01Z</time></a> </td>
            </tr>
            </tbody>
        </table>
    </div>
</article>
<div id="live-events">
    <ol class="LiveEvents live-events">
        <li><a href="/player/?id=chill">Chill completed map ac_elevation</a> <span> </span> <span class="when"> <time
                    is="relative-time" datetime="2016-05-12T21:44:08Z"
                    title="May 13, 2016, 5:44 AM GMT+8">16 hours ago</time> </span></li>
        <li><a href="/player/?id=robaum">Robaum achieved Cube Addict level 50h</a> <span> </span> <span class="when"> <time
                    is="relative-time" datetime="2016-05-12T16:57:38Z"
                    title="May 13, 2016, 12:57 AM GMT+8">21 hours ago</time> </span></li>
        <li><a href="/player/?id=thla">thla achieved Frag Master level 2000</a> <span> </span> <span class="when"> <time
                    is="relative-time" datetime="2016-05-12T13:51:18Z"
                    title="May 12, 2016, 9:51 PM GMT+8">a day ago</time> </span></li>
        <li><a href="/player/?id=forever">forever achieved Frag Master level 1000</a> <span> </span> <span class="when"> <time
                    is="relative-time" datetime="2016-05-11T23:01:28Z"
                    title="May 12, 2016, 7:01 AM GMT+8">2 days ago</time> </span></li>
        <li><a href="/player/?id=thirtytwo">thirtytwo completed map ac_ingress</a> <span> </span> <span class="when"> <time
                    is="relative-time" datetime="2016-05-11T21:23:44Z"
                    title="May 12, 2016, 5:23 AM GMT+8">2 days ago</time> </span></li>
        <li><a href="/player/?id=chill">Chill achieved Frag Master level 5000</a> <span> </span> <span class="when"> <time
                    is="relative-time" datetime="2016-05-11T20:35:03Z"
                    title="May 12, 2016, 4:35 AM GMT+8">2 days ago</time> </span></li>
        <li><a href="/player/?id=zzzzz">Valeriy achieved Flag Master level 100</a> <span> </span> <span class="when"> <time
                    is="relative-time" datetime="2016-05-11T20:19:16Z"
                    title="May 12, 2016, 4:19 AM GMT+8">2 days ago</time> </span></li>
    </ol>
</div>

<div id="latest-clanwars">
    <article class="GameCard game clanwar"
             style="background-image: url('https://cloud.githubusercontent.com/assets/2464813/12814159/d5a55016-cb34-11e5-9eca-2321924b0b4a.png')">
        <div class="w">
            <header>
                <h2><a href="/clanwar/?id=2016-05-11T22:04:54Z">
                        <time is="local-time" datetime="2016-05-11T22:04:54Z" weekday="short" year="numeric"
                              month="short" day="numeric" title="May 12, 2016, 6:04 AM GMT+8">Thu May 12, 2016
                        </time>
                    </a></h2>
            </header>
            <div class="teams">
                <div class="team">
                    <div class="team-header">
                        <h3><a href="/clan/?id=mys"> <img class="clan-logo"
                                                          src="https://cloud.githubusercontent.com/assets/16243477/12007811/08f26e38-ac16-11e5-8855-e23429e226d8.png"
                                                          width="64" height="64"> </a></h3>
                        <div class="result">
                            <span class="clan"><a href="/clan/?id=mys">MyS</a></span>
                            <span class="score">2</span>
                        </div>
                    </div>
                </div>
                <div class="team">
                    <div class="team-header">
                        <h3><a href="/clan/?id=ir"> <img class="clan-logo"
                                                         src="https://cloud.githubusercontent.com/assets/5359646/12004100/caeee00e-ab3d-11e5-8b35-9ed661f5b06f.png"
                                                         width="64" height="64"> </a></h3>
                        <div class="result">
                            <span class="clan"><a href="/clan/?id=ir">iR</a></span>
                            <span class="score">1</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </article>
    <article class="GameCard game clanwar"
             style="background-image: url('https://cloud.githubusercontent.com/assets/2464813/12814159/d5a55016-cb34-11e5-9eca-2321924b0b4a.png')">
        <div class="w">
            <header>
                <h2><a href="/clanwar/?id=2016-05-11T22:04:54Z">
                        <time is="local-time" datetime="2016-05-11T22:04:54Z" weekday="short" year="numeric"
                              month="short" day="numeric" title="May 12, 2016, 6:04 AM GMT+8">Thu May 12, 2016
                        </time>
                    </a></h2>
            </header>
            <div class="teams">
                <div class="team">
                    <div class="team-header">
                        <h3><a href="/clan/?id=mys"> <img class="clan-logo"
                                                          src="https://cloud.githubusercontent.com/assets/16243477/12007811/08f26e38-ac16-11e5-8855-e23429e226d8.png"
                                                          width="64" height="64"> </a></h3>
                        <div class="result">
                            <span class="clan"><a href="/clan/?id=mys">MyS</a></span>
                            <span class="score">2</span>
                        </div>
                    </div>
                </div>
                <div class="team">
                    <div class="team-header">
                        <h3><a href="/clan/?id=ir"> <img class="clan-logo"
                                                         src="https://cloud.githubusercontent.com/assets/5359646/12004100/caeee00e-ab3d-11e5-8b35-9ed661f5b06f.png"
                                                         width="64" height="64"> </a></h3>
                        <div class="result">
                            <span class="clan"><a href="/clan/?id=ir">iR</a></span>
                            <span class="score">1</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </article>
    <article class="GameCard game clanwar"
             style="background-image: url('https://cloud.githubusercontent.com/assets/2464813/12814159/d5a55016-cb34-11e5-9eca-2321924b0b4a.png')">
        <div class="w">
            <header>
                <h2><a href="/clanwar/?id=2016-05-11T22:04:54Z">
                        <time is="local-time" datetime="2016-05-11T22:04:54Z" weekday="short" year="numeric"
                              month="short" day="numeric" title="May 12, 2016, 6:04 AM GMT+8">Thu May 12, 2016
                        </time>
                    </a></h2>
            </header>
            <div class="teams">
                <div class="team">
                    <div class="team-header">
                        <h3><a href="/clan/?id=mys"> <img class="clan-logo"
                                                          src="https://cloud.githubusercontent.com/assets/16243477/12007811/08f26e38-ac16-11e5-8855-e23429e226d8.png"
                                                          width="64" height="64"> </a></h3>
                        <div class="result">
                            <span class="clan"><a href="/clan/?id=mys">MyS</a></span>
                            <span class="score">2</span>
                        </div>
                    </div>
                </div>
                <div class="team">
                    <div class="team-header">
                        <h3><a href="/clan/?id=ir"> <img class="clan-logo"
                                                         src="https://cloud.githubusercontent.com/assets/5359646/12004100/caeee00e-ab3d-11e5-8b35-9ed661f5b06f.png"
                                                         width="64" height="64"> </a></h3>
                        <div class="result">
                            <span class="clan"><a href="/clan/?id=ir">iR</a></span>
                            <span class="score">1</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </article>
</div>
<div id="games">
    <div id="dynamic-games">
        <div id="server-1791137909">


        </div>
        <div id="server-764599927">


        </div>
        <div id="server-1791167700">


        </div>
        <div id="server-1791108118">


        </div>
        <div id="server--1002728126">


        </div>
        <div id="server-1791102164">


        </div>
        <div id="server--1044951151">


        </div>
        <div id="server-1791197491">


        </div>
    </div>
    <div id="new-games"></div>
    <div id="existing-games">
        <article class="GameCard game "
                 style="background-image: url('https://cloud.githubusercontent.com/assets/5359646/12695178/1c42b7dc-c745-11e5-828b-1ea8b1b376f2.jpg')">
            <div class="w">
                <header>
                    <h2><a href="/game/?id=2016-05-13T12:55:56Z"> ctf @ ac_desert3
                            <time is="relative-time" datetime="2016-05-13T14:47:49+02:00"
                                  title="May 13, 2016, 8:47 PM GMT+8">an hour ago
                            </time>
                        </a> <a target="_blank" class="demo-link"
                                href="http://woop.ac:81/find-demo.php?time=2016-05-13T12:55:56Z&amp;map=ac_desert3">demo</a>
                    </h2>
                </header>
                <div class="teams">
                    <div class="RVSF team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695181/369cda90-c745-11e5-96eb-3f3669f80aed.png">
                            </h3>
                            <div class="result">
                                <span class="score">11</span>
                                <span class="subscore">86</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">8</span> <span
                                        class="subscore flags">50</span> <span class="name"><a href="/player/?id=hure">|BC|hure</a></span>
                                </li>
                                <li class="player"><span class="score flags">3</span> <span
                                        class="subscore flags">36</span> <span class="name">Student</span></li>
                            </ol>
                        </div>
                    </div>
                    <div class="CLA team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695180/369c86da-c745-11e5-817f-46d8c4c42376.png">
                            </h3>
                            <div class="result">
                                <span class="score">3</span>
                                <span class="subscore">63</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">2</span> <span
                                        class="subscore flags">41</span> <span class="name"><a
                                            href="/player/?id=zangetsu">Zangetsu|ZZ</a></span></li>
                                <li class="player"><span class="score flags">1</span> <span
                                        class="subscore flags">22</span> <span class="name">|40+|Han.Solo</span></li>
                            </ol>
                        </div>
                    </div>
                </div>
            </div>
        </article>
        <article class="GameCard game "
                 style="background-image: url('https://cloud.githubusercontent.com/assets/5359646/12695157/1bffd958-c745-11e5-83c7-ed9144405076.jpg')">
            <div class="w">
                <header>
                    <h2><a href="/game/?id=2016-05-12T23:52:39Z"> ctf @ ac_ingress
                            <time is="relative-time" datetime="2016-05-13T01:44:34+02:00"
                                  title="May 13, 2016, 7:44 AM GMT+8">14 hours ago
                            </time>
                        </a> <a target="_blank" class="demo-link"
                                href="http://woop.ac:81/find-demo.php?time=2016-05-12T23:52:39Z&amp;map=ac_ingress">demo</a>
                    </h2>
                </header>
                <div class="teams">
                    <div class="CLA team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695180/369c86da-c745-11e5-817f-46d8c4c42376.png">
                            </h3>
                            <div class="result">
                                <span class="score">13</span>
                                <span class="subscore">124</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">8</span> <span
                                        class="subscore flags">55</span> <span class="name"><a
                                            href="/player/?id=rafael">iR|.Rafa*</a></span></li>
                                <li class="player"><span class="score flags">5</span> <span
                                        class="subscore flags">69</span> <span class="name"><a href="/player/?id=enty">iR|.EnTy^</a></span>
                                </li>
                            </ol>
                        </div>
                    </div>
                    <div class="RVSF team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695181/369cda90-c745-11e5-96eb-3f3669f80aed.png">
                            </h3>
                            <div class="result">
                                <span class="score">2</span>
                                <span class="subscore">82</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">2</span> <span
                                        class="subscore flags">46</span> <span class="name"><a
                                            href="/player/?id=zangetsu">Zangetsu|ZZ</a></span></li>
                                <li class="player"><span class="score flags">0</span> <span
                                        class="subscore flags">36</span> <span class="name"><a
                                            href="/player/?id=papacho">PAPACHO</a></span></li>
                            </ol>
                        </div>
                    </div>
                </div>
            </div>
        </article>
        <article class="GameCard game "
                 style="background-image: url('https://cloud.githubusercontent.com/assets/5359646/12831400/58da2ee8-cb94-11e5-9d33-822a5357cb2d.jpg')">
            <div class="w">
                <header>
                    <h2><a href="/game/?id=2016-05-12T23:34:41Z"> ctf @ ac_sunset
                            <time is="relative-time" datetime="2016-05-13T01:26:36+02:00"
                                  title="May 13, 2016, 7:26 AM GMT+8">14 hours ago
                            </time>
                        </a> <a target="_blank" class="demo-link"
                                href="http://woop.ac:81/find-demo.php?time=2016-05-12T23:34:41Z&amp;map=ac_sunset">demo</a>
                    </h2>
                </header>
                <div class="teams">
                    <div class="CLA team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695180/369c86da-c745-11e5-817f-46d8c4c42376.png">
                            </h3>
                            <div class="result">
                                <span class="score">15</span>
                                <span class="subscore">180</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">7</span> <span
                                        class="subscore flags">49</span> <span class="name"><a
                                            href="/player/?id=rafael">iR|.Rafa*</a></span></li>
                                <li class="player"><span class="score flags">5</span> <span
                                        class="subscore flags">61</span> <span class="name"><a href="/player/?id=fury">iR|FURY</a></span>
                                </li>
                                <li class="player"><span class="score flags">3</span> <span
                                        class="subscore flags">70</span> <span class="name"><a href="/player/?id=enty">iR|.EnTy^</a></span>
                                </li>
                            </ol>
                        </div>
                    </div>
                    <div class="RVSF team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695181/369cda90-c745-11e5-96eb-3f3669f80aed.png">
                            </h3>
                            <div class="result">
                                <span class="score">4</span>
                                <span class="subscore">111</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">2</span> <span
                                        class="subscore flags">35</span> <span class="name"><a
                                            href="/player/?id=papacho">PAPACHO</a></span></li>
                                <li class="player"><span class="score flags">1</span> <span
                                        class="subscore flags">49</span> <span class="name"><a
                                            href="/player/?id=zangetsu">Zangetsu|ZZ</a></span></li>
                                <li class="player"><span class="score flags">1</span> <span
                                        class="subscore flags">27</span> <span class="name">Pi_MrPompey14</span></li>
                            </ol>
                        </div>
                    </div>
                </div>
            </div>
        </article>
        <article class="GameCard game "
                 style="background-image: url('https://cloud.githubusercontent.com/assets/5359646/12695178/1c42b7dc-c745-11e5-828b-1ea8b1b376f2.jpg')">
            <div class="w">
                <header>
                    <h2><a href="/game/?id=2016-05-12T23:15:03Z"> ctf @ ac_desert3
                            <time is="relative-time" datetime="2016-05-12T23:06:58Z"
                                  title="May 13, 2016, 7:06 AM GMT+8">15 hours ago
                            </time>
                        </a></h2>
                </header>
                <div class="teams">
                    <div class="RVSF team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695181/369cda90-c745-11e5-96eb-3f3669f80aed.png">
                            </h3>
                            <div class="result">
                                <span class="score">20</span>
                                <span class="subscore">68</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">12</span> <span
                                        class="subscore flags">27</span> <span class="name">~|TAA|~Giberman</span></li>
                                <li class="player"><span class="score flags">8</span> <span
                                        class="subscore flags">41</span> <span class="name">*[BRC]*Wall-E</span></li>
                            </ol>
                        </div>
                    </div>
                    <div class="CLA team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695180/369c86da-c745-11e5-817f-46d8c4c42376.png">
                            </h3>
                            <div class="result">
                                <span class="score">0</span>
                                <span class="subscore">36</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">0</span> <span
                                        class="subscore flags">25</span> <span class="name"><a
                                            href="/player/?id=suicidesquad">~|TAA|~Suicide</a></span></li>
                                <li class="player"><span class="score flags">0</span> <span
                                        class="subscore flags">11</span> <span class="name">BirD^</span></li>
                            </ol>
                        </div>
                    </div>
                </div>
            </div>
        </article>
        <article class="GameCard game "
                 style="background-image: url('https://cloud.githubusercontent.com/assets/5359646/12695178/1c42b7dc-c745-11e5-828b-1ea8b1b376f2.jpg')">
            <div class="w">
                <header>
                    <h2><a href="/game/?id=2016-05-12T22:52:10Z"> ctf @ ac_desert3
                            <time is="relative-time" datetime="2016-05-13T00:44:05+02:00"
                                  title="May 13, 2016, 6:44 AM GMT+8">15 hours ago
                            </time>
                        </a> <a target="_blank" class="demo-link"
                                href="http://woop.ac:81/find-demo.php?time=2016-05-12T22:52:10Z&amp;map=ac_desert3">demo</a>
                    </h2>
                </header>
                <div class="teams">
                    <div class="CLA team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695180/369c86da-c745-11e5-817f-46d8c4c42376.png">
                            </h3>
                            <div class="result">
                                <span class="score">9</span>
                                <span class="subscore">78</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">5</span> <span
                                        class="subscore flags">42</span> <span class="name"><a href="/player/?id=chill">.45|Chill</a></span>
                                </li>
                                <li class="player"><span class="score flags">4</span> <span
                                        class="subscore flags">36</span> <span class="name"><a
                                            href="/player/?id=zangetsu">Zangetsu|ZZ</a></span></li>
                            </ol>
                        </div>
                    </div>
                    <div class="RVSF team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695181/369cda90-c745-11e5-96eb-3f3669f80aed.png">
                            </h3>
                            <div class="result">
                                <span class="score">5</span>
                                <span class="subscore">74</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">4</span> <span
                                        class="subscore flags">50</span> <span class="name"><a href="/player/?id=enty">iR|.EnTy^</a></span>
                                </li>
                                <li class="player"><span class="score flags">1</span> <span
                                        class="subscore flags">24</span> <span class="name">Pi_MrPompey14</span></li>
                            </ol>
                        </div>
                    </div>
                </div>
            </div>
        </article>
        <article class="GameCard game "
                 style="background-image: url('https://cloud.githubusercontent.com/assets/5359646/12695157/1bffd958-c745-11e5-83c7-ed9144405076.jpg')">
            <div class="w">
                <header>
                    <h2><a href="/game/?id=2016-05-12T22:41:56Z"> ctf @ ac_ingress
                            <time is="relative-time" datetime="2016-05-12T22:33:51Z"
                                  title="May 13, 2016, 6:33 AM GMT+8">15 hours ago
                            </time>
                        </a></h2>
                </header>
                <div class="teams">
                    <div class="RVSF team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695181/369cda90-c745-11e5-96eb-3f3669f80aed.png">
                            </h3>
                            <div class="result">
                                <span class="score">8</span>
                                <span class="subscore">101</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">6</span> <span
                                        class="subscore flags">37</span> <span class="name">eva</span></li>
                                <li class="player"><span class="score flags">2</span> <span
                                        class="subscore flags">64</span> <span class="name">Wall-E</span></li>
                            </ol>
                        </div>
                    </div>
                    <div class="CLA team">
                        <div class="team-header">
                            <h3><img
                                    src="https://cloud.githubusercontent.com/assets/5359646/12695180/369c86da-c745-11e5-817f-46d8c4c42376.png">
                            </h3>
                            <div class="result">
                                <span class="score">6</span>
                                <span class="subscore">77</span>
                            </div>
                        </div>
                        <div class="players">
                            <ol>
                                <li class="player"><span class="score flags">5</span> <span
                                        class="subscore flags">38</span> <span class="name">GEMA!</span></li>
                                <li class="player"><span class="score flags">1</span> <span
                                        class="subscore flags">39</span> <span class="name"><a
                                            href="/player/?id=suicidesquad">~|TAA|~Suicide</a></span></li>
                            </ol>
                        </div>
                    </div>
                </div>
            </div>
        </article>

    </div>


    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
    <script src="/assets/live/live.js"></script>
    <p id="view-json"><a href="?format=json">View JSON</a></p></div>


<article id="clan-ranks" class="bordered">
    <div id="rank">
        <h2>Clan Ranks</h2>
        <table style="width: 480px;">
            <tbody>
            <tr>
                <th>Clan</th>
                <td>Wars</td>
                <td>Won</td>
                <td>Games</td>
                <td>Score</td>
                <td>Elo Rank</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=brc">BRC</a></th>
                <td>65</td>
                <td>31</td>
                <td>137</td>
                <td>186487</td>
                <td>16</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=woop">w00p</a></th>
                <td>50</td>
                <td>33</td>
                <td>106</td>
                <td>175527</td>
                <td>2</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=mys">MyS</a></th>
                <td>40</td>
                <td>21</td>
                <td>91</td>
                <td>126511</td>
                <td>6</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=ir">iR</a></th>
                <td>39</td>
                <td>23</td>
                <td>86</td>
                <td>154126</td>
                <td>3</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=dnc">DnC</a></th>
                <td>28</td>
                <td>12</td>
                <td>60</td>
                <td>73751</td>
                <td>14</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=rc">rC</a></th>
                <td>26</td>
                <td>25</td>
                <td>55</td>
                <td>106793</td>
                <td>1</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=pi">Pi</a></th>
                <td>23</td>
                <td>9</td>
                <td>48</td>
                <td>60294</td>
                <td>12</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=fel">FEL</a></th>
                <td>18</td>
                <td>3</td>
                <td>36</td>
                <td>39291</td>
                <td>21</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=ax">4X</a></th>
                <td>16</td>
                <td>6</td>
                <td>36</td>
                <td>42559</td>
                <td>13</td>
            </tr>
            <tr>
                <th><a href="/clan/?id=bc">BC</a></th>
                <td>12</td>
                <td>4</td>
                <td>25</td>
                <td>26246</td>
                <td>15</td>
            </tr>
            </tbody>
        </table>
    </div>
</article>
<?php echo $foot; ?>
