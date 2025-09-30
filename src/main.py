import os
import argparse


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--scenario", type=str, default="1")
    args = parser.parse_args()
    if args.scenario == "1":
        print("Scenario 1")
    elif args.scenario == "2":
        print("Scenario 2")
    elif args.scenario == "3":
        print("Scenario 3")
    else:
        print("Invalid scenario")
    
    