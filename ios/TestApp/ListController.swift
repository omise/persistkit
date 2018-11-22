import UIKit
import PersistKit

class ListController: UIViewController {
    let viewModel = ListViewModel()

    @IBOutlet var addButton: UIBarButtonItem! = nil

    var tableView: UITableView { return view as! UITableView }

    override func viewDidLoad() {
        super.viewDidLoad()
        self.title = "PersistKit Example"
        self.navigationItem.rightBarButtonItem = addButton
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        tableView.reloadData()
    }

    @IBAction func didTapAdd(_ sender: Any) {
        let alertController = UIAlertController(
            title: "Add New TODO",
            message: "Type the description.",
            preferredStyle: UIAlertController.Style.alert)

        alertController.addTextField { (textField) in textField.text = "(description)" }
        alertController.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alertController.addAction(UIAlertAction(title: "Confirm", style: .default, handler: { [weak self] (action) in
            DispatchQueue.main.async {
                guard let description = alertController.textFields?.first?.text else { return }
                self?.viewModel.add(item: TodoItem(new: description))
                self?.tableView.reloadData()
            }
        }))

        self.present(alertController, animated: true, completion: nil)
    }
}

extension ListController: UITableViewDataSource {
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1;
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.viewModel.todoItems.count
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "cell") ??
            UITableViewCell(style: .default, reuseIdentifier: "cell")

        let todoItem = self.viewModel.todoItems[indexPath.row]
        cell.textLabel?.text = todoItem.description
        
        if todoItem.completed {
            cell.textLabel?.textColor = UIColor.lightGray
        } else {
            cell.textLabel?.textColor = UIColor.black
        }
        return cell
    }
}

extension ListController: UITableViewDelegate {
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let todoItem = self.viewModel.todoItems[indexPath.row]
        _ = self.viewModel.toggle(item: todoItem)
        tableView.reloadData()
    }
}
