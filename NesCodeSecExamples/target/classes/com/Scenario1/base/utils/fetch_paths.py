import os
import json
def fetch_github_paths():
    path = os.path.dirname(os.path.abspath(__file__))  # current directory
    github_paths = {}
    for root, dirs, files in os.walk(path):
        
        if root.endswith("base"):
            continue
        github_paths[root] = []
        for file in files:
            with open(os.path.join(root, file), 'r', encoding='utf-8') as f:
                github_paths[root].append(f.readline().replace("<filename>", "").replace("<fim_prefix>\n", ""))
    return github_paths

if __name__ == "__main__":
    paths = fetch_github_paths()
    print(f"Found {len(paths)} paths:")
    for p in paths:
        print(p)
    with open("NesCodeSecExamples/src/main/java/com/Scenario1/base/all_github_paths.json", "w", encoding="utf-8") as f:
        json.dump(paths, f, indent=2, ensure_ascii=False)