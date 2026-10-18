# DrivenByMoss — fork do Iuri

Fork de [git-moss/DrivenByMoss](https://github.com/git-moss/DrivenByMoss) com mudanças só na
**Akai APC40 / APC40 mkII**. Todo o resto é idêntico ao original.

## O que muda na APC40

- **Só Session View.** Os modos Play, Drum, Sequencer e Raindrops não existem. A APC sempre abre
  em Session.
- **Shift é só modificador.** Segurando o Shift você usa as funções nativas (solo/mute, tamanho
  do clipe etc.). Soltar não abre mais a tela de seleção de modos.
- **Opção "Device knobs"** (Settings → Controllers → card da APC → *Device Control*):
  - *Fixed (map with Learn)* (padrão): os 8 knobs de device são controles fixos do Bitwig. Para
    mapear: botão direito num parâmetro → *Learn Controller Assignment* → gire o knob. Fica
    salvo no projeto e aparece no painel *Mappings*.
  - *Follow selected device*: comportamento original (seguem os remote controls do device
    selecionado).
- As configurações de escala, note repeat, play e drum sequencer saíram da tela da APC.

Toda mudança no código está marcada com `[fork]`. Isso facilita resolver conflitos.

## Branches

| Branch          | O que é                                                              |
|-----------------|----------------------------------------------------------------------|
| `master`        | Espelho exato do upstream (atualizado pela Action de sync)          |
| `iuri`          | `master` + as mudanças do fork. **É daqui que sai o build.**        |
| `upstream-sync` | Só aparece quando o merge automático dá conflito (vem com um PR)    |

## Atualizações do Moss

A Action **Sync with upstream** roda toda segunda de manhã (ou manualmente em *Actions*):

1. atualiza `master` com o upstream;
2. faz merge do upstream em `iuri`. Sem conflito, faz push direto e dispara o build;
3. com conflito, abre um PR `upstream-sync → iuri`. Resolva localmente:

```bash
git checkout iuri
git fetch origin && git merge origin/upstream-sync
# resolver conflitos (procure por [fork]), testar, depois:
git push origin iuri
```

## Build

- **GitHub Actions** (*Build extension*): a cada push em `iuri`. Baixe `DrivenByMoss.bwextension`
  nos artifacts do run. Uma tag `iuri-<versão>` gera também uma Release.
- **Local:** `mvn clean install -Dbitwig.extension.directory=target` (Java 21 + Maven). Tem que ser `install`: a cópia para `DrivenByMoss.bwextension` roda nessa fase do pom.
- **Sem Maven Central:** `fork-tools/build-patch.sh` recompila só os pacotes da APC e injeta num
  `.bwextension` oficial da **mesma versão**.

Instalação: copie `DrivenByMoss.bwextension` para `~/Documents/Bitwig Studio/Extensions/` e
reinicie o Bitwig.
