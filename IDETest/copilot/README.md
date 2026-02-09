# IDETest Copilot Testcases

This directory contains instantiated testcases for one of the four commertial NES products, the GitHub Copilot black-box NES model. Each risk vector contains a NES reponse trace obtained from Visual Studio Code Developer Chat Debug View for one of the test cases.

## Layout

- `V1/` to `V12/`: individual testcase sets.
- Each `V*/base/` folder typically holds the baseline code used for the prompt.
- Each `V*/copilot_all_prompts_*.chatreplay.json` and `*.copilotmd` file stores the captured Copilot NES prompt/response replay for that testcase version.

## Notes

- Testcases are organized by version number only; consult each `V*/` folder for scenario-specific details and artifacts.
- Files are captured as-is to preserve the experiment conditions.
